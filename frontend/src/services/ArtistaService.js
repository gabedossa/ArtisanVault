import axios from "axios"

const API_URL = 'http://localhost:8080/api/artistas';

export const createArtista = async (artista) => {
    try {
      const response = await axios.post(`${API_URL}/post`, artista);
      return response.data;
    } catch (error) {
      console.error('Erro ao criar artista:', error);
      throw error;
    }
  };

export const getAllArtista = async () => {
    try{
        const response = await axios.get(API_URL);
        return response.data;
    } catch(e){
        console.log('Erro ao buscar artista', e);
        throw e;
    }
}

console.log(getAllArtista);