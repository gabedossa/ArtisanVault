import './App.css';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import Login from './paginas/Login/Login';
import Cadastro from './paginas/Cadastro/Cadastro';

function App() {
  return (
    <div className="App">
      <header className="App-header">
      <Router>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
        </Routes>
    </Router>
      </header>
    </div>
  );
}

export default App;