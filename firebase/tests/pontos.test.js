// Testes das regras de segurança do ponto do funcionário: pontos/{uid}/registros/{dia}_{tipo}
// Rodam contra o emulador local do Firestore: npm run test:regras
import { after, before, beforeEach, describe, it } from 'node:test';
import { readFileSync } from 'node:fs';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import {
  Timestamp,
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
} from 'firebase/firestore';

const UID_FUNCIONARIO = 'funcionario-1';
const UID_OUTRO_FUNCIONARIO = 'funcionario-2';
const UID_PACIENTE = 'paciente-1';
const DIA = '2026-11-10';

let ambiente;

function banco(uid) {
  return ambiente.authenticatedContext(uid).firestore();
}

// Referência do registro "dia_tipo" dentro do ponto de um funcionário
function registro(db, tipo, uid = UID_FUNCIONARIO, dia = DIA) {
  return doc(db, 'pontos', uid, 'registros', `${dia}_${tipo}`);
}

// Dados válidos, iguais aos que o app envia
function dadosValidos(tipo, dia = DIA) {
  return { tipo, dia, horario: serverTimestamp() };
}

// Grava um registro "pela administração", sem passar pelas regras (para montar o cenário)
async function jaRegistrado(tipo, uid = UID_FUNCIONARIO) {
  await ambiente.withSecurityRulesDisabled(async (contexto) => {
    await setDoc(registro(contexto.firestore(), tipo, uid), { tipo, dia: DIA, horario: Timestamp.now() });
  });
}

before(async () => {
  ambiente = await initializeTestEnvironment({
    projectId: 'demo-bemmequer-pontos',
    firestore: { rules: readFileSync('firestore.rules', 'utf8') },
  });
});

after(async () => {
  await ambiente.cleanup();
});

beforeEach(async () => {
  await ambiente.clearFirestore();
  await ambiente.withSecurityRulesDisabled(async (contexto) => {
    const db = contexto.firestore();
    await setDoc(doc(db, 'usuarios', UID_FUNCIONARIO), { tipo: 'funcionario', nome: 'Nicolle' });
    await setDoc(doc(db, 'usuarios', UID_OUTRO_FUNCIONARIO), { tipo: 'funcionario', nome: 'Carlos' });
    await setDoc(doc(db, 'usuarios', UID_PACIENTE), { tipo: 'paciente', nomePaciente: 'Rafael Souza' });
  });
});

describe('bater o ponto', () => {
  it('permite o funcionário registrar a entrada', async () => {
    const db = banco(UID_FUNCIONARIO);
    await assertSucceeds(setDoc(registro(db, 'entrada'), dadosValidos('entrada')));
  });

  it('permite o dia completo, na ordem certa', async () => {
    const db = banco(UID_FUNCIONARIO);
    for (const tipo of ['entrada', 'almoco_inicio', 'almoco_fim', 'intervalo_inicio', 'intervalo_fim', 'saida']) {
      await assertSucceeds(setDoc(registro(db, tipo), dadosValidos(tipo)));
    }
  });

  it('bloqueia registrar o mesmo tipo duas vezes no dia', async () => {
    await jaRegistrado('entrada');
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'entrada'), dadosValidos('entrada')));
  });

  it('bloqueia iniciar o almoço sem ter registrado a entrada', async () => {
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'almoco_inicio'), dadosValidos('almoco_inicio')));
  });

  it('bloqueia finalizar o almoço sem ter iniciado', async () => {
    await jaRegistrado('entrada');
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'almoco_fim'), dadosValidos('almoco_fim')));
  });

  it('bloqueia finalizar o intervalo sem ter iniciado', async () => {
    await jaRegistrado('entrada');
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'intervalo_fim'), dadosValidos('intervalo_fim')));
  });

  it('bloqueia o fim de expediente sem entrada', async () => {
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'saida'), dadosValidos('saida')));
  });

  it('bloqueia horário escolhido pelo celular (tem que ser o do servidor)', async () => {
    const db = banco(UID_FUNCIONARIO);
    const dados = { ...dadosValidos('entrada'), horario: Timestamp.fromDate(new Date('2026-11-10T08:00:00')) };
    await assertFails(setDoc(registro(db, 'entrada'), dados));
  });

  it('bloqueia ID que não bate com o dia e o tipo', async () => {
    const db = banco(UID_FUNCIONARIO);
    const outroId = doc(db, 'pontos', UID_FUNCIONARIO, 'registros', 'qualquer-coisa');
    await assertFails(setDoc(outroId, dadosValidos('entrada')));
  });

  it('bloqueia tipo desconhecido', async () => {
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'hora_extra'), dadosValidos('hora_extra')));
  });

  it('bloqueia dia fora do formato AAAA-MM-DD', async () => {
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'entrada', UID_FUNCIONARIO, '10/11/2026'),
      dadosValidos('entrada', '10/11/2026')));
  });

  it('bloqueia campos extras (ex.: localização, ainda não prevista)', async () => {
    const db = banco(UID_FUNCIONARIO);
    const dados = { ...dadosValidos('entrada'), latitude: -23.5 };
    await assertFails(setDoc(registro(db, 'entrada'), dados));
  });

  it('bloqueia bater o ponto de outro funcionário', async () => {
    const db = banco(UID_FUNCIONARIO);
    await assertFails(setDoc(registro(db, 'entrada', UID_OUTRO_FUNCIONARIO), dadosValidos('entrada')));
  });

  it('bloqueia paciente bater ponto', async () => {
    const db = banco(UID_PACIENTE);
    await assertFails(setDoc(registro(db, 'entrada', UID_PACIENTE), dadosValidos('entrada')));
  });

  it('bloqueia usuário não logado', async () => {
    const db = ambiente.unauthenticatedContext().firestore();
    await assertFails(setDoc(registro(db, 'entrada'), dadosValidos('entrada')));
  });
});

describe('ler o ponto', () => {
  beforeEach(async () => {
    await jaRegistrado('entrada');
  });

  it('permite o funcionário ler o próprio registro', async () => {
    await assertSucceeds(getDoc(registro(banco(UID_FUNCIONARIO), 'entrada')));
  });

  it('permite o funcionário listar os próprios últimos dias', async () => {
    const db = banco(UID_FUNCIONARIO);
    const consulta = query(collection(db, 'pontos', UID_FUNCIONARIO, 'registros'), where('dia', '>=', '2026-10-27'));
    await assertSucceeds(getDocs(consulta));
  });

  it('bloqueia outro funcionário ler o ponto', async () => {
    await assertFails(getDoc(registro(banco(UID_OUTRO_FUNCIONARIO), 'entrada')));
  });

  it('bloqueia outro funcionário listar o ponto', async () => {
    const db = banco(UID_OUTRO_FUNCIONARIO);
    await assertFails(getDocs(collection(db, 'pontos', UID_FUNCIONARIO, 'registros')));
  });

  it('bloqueia paciente ler o ponto', async () => {
    await assertFails(getDoc(registro(banco(UID_PACIENTE), 'entrada')));
  });
});

describe('alterar ou apagar o ponto', () => {
  beforeEach(async () => {
    await jaRegistrado('entrada');
  });

  it('bloqueia o funcionário alterar o horário', async () => {
    const db = banco(UID_FUNCIONARIO);
    await assertFails(updateDoc(registro(db, 'entrada'), { horario: serverTimestamp() }));
  });

  it('bloqueia o funcionário apagar um registro', async () => {
    await assertFails(deleteDoc(registro(banco(UID_FUNCIONARIO), 'entrada')));
  });
});
