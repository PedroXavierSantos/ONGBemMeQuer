// Testes das regras de segurança da coleção "usuarios".
// Rodam contra o emulador local do Firestore: npm run test:regras
import { after, before, beforeEach, describe, it } from 'node:test';
import { readFileSync } from 'node:fs';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import { doc, getDoc, serverTimestamp, setDoc, updateDoc, deleteDoc } from 'firebase/firestore';

const UID_PACIENTE = 'paciente-1';
const UID_OUTRO_PACIENTE = 'paciente-2';
const UID_FUNCIONARIO = 'funcionario-1';

let ambiente;

// Monta um cadastro válido, igual ao que o app envia na tela "Cadastro (2)"
function cadastroValido(email) {
  return {
    tipo: 'paciente',
    nomeResponsavel: 'Maria Souza',
    telefone: '11999998888',
    email,
    nomePaciente: 'Rafael Souza',
    descricaoPaciente: '',
    intencoes: 'Fisioterapia',
    criadoEm: serverTimestamp(),
  };
}

function bancoDoPaciente() {
  return ambiente.authenticatedContext(UID_PACIENTE, { email: 'maria@exemplo.com' }).firestore();
}

before(async () => {
  ambiente = await initializeTestEnvironment({
    projectId: 'demo-bemmequer',
    firestore: { rules: readFileSync('firestore.rules', 'utf8') },
  });
});

after(async () => {
  await ambiente.cleanup();
});

beforeEach(async () => {
  await ambiente.clearFirestore();
  // Dados iniciais criados "pela administração" (sem passar pelas regras)
  await ambiente.withSecurityRulesDisabled(async (contexto) => {
    const db = contexto.firestore();
    await setDoc(doc(db, 'usuarios', UID_FUNCIONARIO), { tipo: 'funcionario', email: 'func@exemplo.com' });
    await setDoc(doc(db, 'usuarios', UID_OUTRO_PACIENTE), { tipo: 'paciente', email: 'joao@exemplo.com' });
  });
});

describe('cadastro (create)', () => {
  it('permite o paciente criar o próprio perfil', async () => {
    const db = bancoDoPaciente();
    await assertSucceeds(setDoc(doc(db, 'usuarios', UID_PACIENTE), cadastroValido('maria@exemplo.com')));
  });

  it('aceita o e-mail com letras maiúsculas diferentes do token', async () => {
    const db = bancoDoPaciente();
    await assertSucceeds(setDoc(doc(db, 'usuarios', UID_PACIENTE), cadastroValido('Maria@Exemplo.com')));
  });

  it('bloqueia se cadastrar como funcionário', async () => {
    const db = bancoDoPaciente();
    const dados = { ...cadastroValido('maria@exemplo.com'), tipo: 'funcionario' };
    await assertFails(setDoc(doc(db, 'usuarios', UID_PACIENTE), dados));
  });

  it('bloqueia criar o perfil de outra pessoa', async () => {
    const db = bancoDoPaciente();
    await assertFails(setDoc(doc(db, 'usuarios', 'outro-uid'), cadastroValido('maria@exemplo.com')));
  });

  it('bloqueia usar um e-mail diferente do da conta', async () => {
    const db = bancoDoPaciente();
    await assertFails(setDoc(doc(db, 'usuarios', UID_PACIENTE), cadastroValido('outra@exemplo.com')));
  });

  it('bloqueia campos extras não previstos', async () => {
    const db = bancoDoPaciente();
    const dados = { ...cadastroValido('maria@exemplo.com'), admin: true };
    await assertFails(setDoc(doc(db, 'usuarios', UID_PACIENTE), dados));
  });

  it('bloqueia nome do paciente vazio', async () => {
    const db = bancoDoPaciente();
    const dados = { ...cadastroValido('maria@exemplo.com'), nomePaciente: '' };
    await assertFails(setDoc(doc(db, 'usuarios', UID_PACIENTE), dados));
  });

  it('bloqueia usuário não logado', async () => {
    const db = ambiente.unauthenticatedContext().firestore();
    await assertFails(setDoc(doc(db, 'usuarios', UID_PACIENTE), cadastroValido('maria@exemplo.com')));
  });
});

describe('leitura (read)', () => {
  it('permite o paciente ler o próprio perfil', async () => {
    const db = bancoDoPaciente();
    await assertSucceeds(getDoc(doc(db, 'usuarios', UID_PACIENTE)));
  });

  it('bloqueia o paciente ler o perfil de outro paciente', async () => {
    const db = bancoDoPaciente();
    await assertFails(getDoc(doc(db, 'usuarios', UID_OUTRO_PACIENTE)));
  });

  it('permite o funcionário ler o perfil dos pacientes', async () => {
    const db = ambiente.authenticatedContext(UID_FUNCIONARIO).firestore();
    await assertSucceeds(getDoc(doc(db, 'usuarios', UID_OUTRO_PACIENTE)));
  });

  it('bloqueia usuário não logado', async () => {
    const db = ambiente.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(db, 'usuarios', UID_OUTRO_PACIENTE)));
  });
});

describe('edição e exclusão (update/delete)', () => {
  beforeEach(async () => {
    await setDoc(doc(bancoDoPaciente(), 'usuarios', UID_PACIENTE), cadastroValido('maria@exemplo.com'));
  });

  it('permite o paciente editar o telefone', async () => {
    const db = bancoDoPaciente();
    await assertSucceeds(updateDoc(doc(db, 'usuarios', UID_PACIENTE), { telefone: '11988887777' }));
  });

  it('bloqueia o paciente se promover a funcionário', async () => {
    const db = bancoDoPaciente();
    await assertFails(updateDoc(doc(db, 'usuarios', UID_PACIENTE), { tipo: 'funcionario' }));
  });

  it('bloqueia trocar o e-mail pelo perfil', async () => {
    const db = bancoDoPaciente();
    await assertFails(updateDoc(doc(db, 'usuarios', UID_PACIENTE), { email: 'novo@exemplo.com' }));
  });

  it('bloqueia o paciente editar o perfil de outro paciente', async () => {
    const db = bancoDoPaciente();
    await assertFails(updateDoc(doc(db, 'usuarios', UID_OUTRO_PACIENTE), { telefone: '11900000000' }));
  });

  it('bloqueia excluir o perfil pelo app', async () => {
    const db = bancoDoPaciente();
    await assertFails(deleteDoc(doc(db, 'usuarios', UID_PACIENTE)));
  });
});
