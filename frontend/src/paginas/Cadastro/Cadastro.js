
import React, {useEffect, useState} from 'react';

import { Link } from 'react-router-dom';
import Logo from '../../component/Logo/Logo';
import './Cadastro.css'
/*Import dos componentes para teste*/
import { ArtistaTable } from '../../component/artistaTable/ArtistaTable';

function Cadastro() {
    return (
      <div className="App">
        <header className="App-header">
          <div className="cadastroCard">
          <p> <Link to="/" className='backBtn'> Voltar </Link></p>
            <Logo/>
            <p className='boxTitle'>Cadastro</p>
            <input placeholder='Nome' className='input_box'/>
            <input placeholder='Username' className='input_box'/>
            <input placeholder='E-mail' className='input_box'/>
            <input placeholder='Descrição' className='input_box'/>
            <input placeholder='Senha' className='input_box'/>
            <input placeholder='Confirmar senha' className='input_box'/>
            <select className='input_box' id="options">
            <option value="">Tipo de conta</option>
            <option value="Artista">ARTISTA</option>
            <option value="Comum">COMUM</option>
          </select>
            <button className='CadastroBtn'>Cadastrar</button>
          </div>
        <ArtistaTable/>
        </header>
      </div>
    );
  }
  
  export default Cadastro;