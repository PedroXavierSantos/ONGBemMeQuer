// Testes das regras de segurança da coleção "relatorios".
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
  addDoc,
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
const UID_PACIENTE = 'paciente-1';
const UID_OUTRO_PACIENTE = 'paciente-2';
const ID_RELATORIO = 'relatorio-1';

let ambiente;

// Monta um relatório válido, igual ao que o app do funcionário envia
function relatorioValido(pacienteUid = UID_PACIENTE) {
  return {
    pacienteUid,
    nomePaciente: 'Rafael Souza',
    funcionarioUid: UID_FUNCIONARIO,
    nomeFuncionario: 'Nicolle',
    relato: 'Participou bem da atividade de pintura.',
    data: Timestamp.fromDate(new Date('2026-10-30T00:00:00')),
    local: 'ONG',
    criadoEm: serverTimestamp(),
  };
}

function bancoDoFuncionario() {
  return ambiente.authenticatedContext(UID_FUNCIONARIO).firestore();
}

function bancoDoPaciente(uid = UID_PACIENTE) {
  return ambiente.authenticatedContext(uid).firestore();
}

before(async () => {
  ambiente = await initializeTestEnvironment({
    projectId: 'demo-bemmequer-relatorios',
    firestore: { rules: readFileSync('firestore.rules', 'utf8') },
  });
});

after(async () => {
  await ambiente.cleanup();
});

beforeEach(async () => {
  await ambiente.clearFirestore();
  // Perfis e um relatório existente criados "pela administração" (sem passar pelas regras)
  await ambiente.withSecurityRulesDisabled(async (contexto) => {
    const db = contexto.firestore();
    await setDoc(doc(db, 'usuarios', UID_FUNCIONARIO), { tipo: 'funcionario', nome: 'Nicolle' });
    await setDoc(doc(db, 'usuarios', UID_PACIENTE), { tipo: 'paciente', nomePaciente: 'Rafael Souza' });
    await setDoc(doc(db, 'usuarios', UID_OUTRO_PACIENTE), { tipo: 'paciente', nomePaciente: 'Ana' });
    await setDoc(doc(db, 'relatorios', ID_RELATORIO), {
      ...relatorioValido(),
      criadoEm: Timestamp.now(),
    });
  });
});

describe('criação de relatório', () => {
  it('permite o funcionário criar um relatório', async () => {
    await assertSucceeds(addDoc(collection(bancoDoFuncionario(), 'relatorios'), relatorioValido()));
  });

  it('bloqueia o paciente criar relatório', async () => {
    const dados = { ...relatorioValido(), funcionarioUid: UID_PACIENTE };
    await assertFails(addDoc(collection(bancoDoPaciente(), 'relatorios'), dados));
  });

  it('bloqueia o funcionário assinar como outra pessoa', async () => {
    const dados = { ...relatorioValido(), funcionarioUid: 'outro-funcionario' };
    await assertFails(addDoc(collection(bancoDoFuncionario(), 'relatorios'), dados));
  });

  it('bloqueia relatório para quem não é paciente', async () => {
    await assertFails(addDoc(collection(bancoDoFuncionario(), 'relatorios'), relatorioValido(UID_FUNCIONARIO)));
  });

  it('bloqueia relatório para paciente que não existe', async () => {
    await assertFails(addDoc(collection(bancoDoFuncionario(), 'relatorios'), relatorioValido('nao-existe')));
  });

  it('bloqueia relato vazio', async () => {
    const dados = { ...relatorioValido(), relato: '' };
    await assertFails(addDoc(collection(bancoDoFuncionario(), 'relatorios'), dados));
  });

  it('bloqueia data em formato de texto', async () => {
    const dados = { ...relatorioValido(), data: '30/10/2026' };
    await assertFails(addDoc(collection(bancoDoFuncionario(), 'relatorios'), dados));
  });

  it('bloqueia campos extras não previstos', async () => {
    const dados = { ...relatorioValido(), urgente: true };
    await assertFails(addDoc(collection(bancoDoFuncionario(), 'relatorios'), dados));
  });
});

describe('leitura de relatórios', () => {
  it('permite a família ler um relatório do seu paciente', async () => {
    await assertSucceeds(getDoc(doc(bancoDoPaciente(), 'relatorios', ID_RELATORIO)));
  });

  it('bloqueia outra família ler o relatório', async () => {
    await assertFails(getDoc(doc(bancoDoPaciente(UID_OUTRO_PACIENTE), 'relatorios', ID_RELATORIO)));
  });

  it('permite a família listar filtrando pelo próprio UID', async () => {
    const consulta = query(collection(bancoDoPaciente(), 'relatorios'), where('pacienteUid', '==', UID_PACIENTE));
    await assertSucceeds(getDocs(consulta));
  });

  it('bloqueia a família listar todos os relatórios', async () => {
    await assertFails(getDocs(collection(bancoDoPaciente(), 'relatorios')));
  });

  it('bloqueia a família listar os relatórios de outra família', async () => {
    const consulta = query(collection(bancoDoPaciente(), 'relatorios'), where('pacienteUid', '==', UID_OUTRO_PACIENTE));
    await assertFails(getDocs(consulta));
  });

  it('permite o funcionário ler e listar relatórios', async () => {
    await assertSucceeds(getDocs(collection(bancoDoFuncionario(), 'relatorios')));
  });

  it('bloqueia usuário não logado', async () => {
    const db = ambiente.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(db, 'relatorios', ID_RELATORIO)));
  });
});

describe('edição e exclusão de relatório', () => {
  it('bloqueia o funcionário editar um relatório entregue', async () => {
    await assertFails(updateDoc(doc(bancoDoFuncionario(), 'relatorios', ID_RELATORIO), { relato: 'alterado' }));
  });

  it('bloqueia excluir um relatório', async () => {
    await assertFails(deleteDoc(doc(bancoDoFuncionario(), 'relatorios', ID_RELATORIO)));
  });
});
