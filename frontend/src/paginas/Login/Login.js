import { Link } from 'react-router-dom';
import './Login.css';
import Logo from '../../component/Logo/Logo.js';
function Login() {
    return (
      <div className="App">
        <header className="App-header">
          <div className="logincard">
            <div className='leftBox'></div>
            <div className='rightBox'>
              <div className='logo'>
              <Logo/>
            </div>
            <input className='inputBox' placeholder='Login'/>
            <input className='inputBox' placeholder='Senha'/>
            <div className='clear'/>
            <button className='btn_login'>Login</button>
            <div className='clear'/>

            <p className='bottomTxt'>Novo por aqui? <Link to="/cadastro" className='bottomTxtBTN'>Clique aqui</Link></p>
          </div>
          </div>
        </header>
      </div>
    );
  }
  
  export default Login;