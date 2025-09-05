import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

interface LoginResponse {
  success: boolean;
  token: string;
  user: {
    id: string;
    email: string;
    name: string;
    role: string;
  };
}

interface User {
  id: string;
  email: string;
  name: string;
  role: string;
}

class AuthService {
  private apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  constructor() {
    // Interceptor para adicionar token automaticamente
    this.apiClient.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Interceptor para lidar com respostas de erro
    this.apiClient.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Token expirado ou inválido
          localStorage.removeItem('authToken');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  async login(email: string, password: string): Promise<LoginResponse> {
    // Credenciais mock para desenvolvimento
    const mockCredentials = [
      {
        email: 'admin@accountabot.com',
        password: 'admin123',
        user: {
          id: '1',
          email: 'admin@accountabot.com',
          name: 'Administrador',
          role: 'admin'
        }
      },
      {
        email: 'user@accountabot.com',
        password: 'user123',
        user: {
          id: '2',
          email: 'user@accountabot.com',
          name: 'Usuário Demo',
          role: 'user'
        }
      },
      {
        email: 'demo@demo.com',
        password: '123456',
        user: {
          id: '3',
          email: 'demo@demo.com',
          name: 'Demo User',
          role: 'user'
        }
      }
    ];

    // Simular delay de rede
    await new Promise(resolve => setTimeout(resolve, 1000));

    // Verificar credenciais mock
    const mockUser = mockCredentials.find(
      cred => cred.email === email && cred.password === password
    );

    if (mockUser) {
      return {
        success: true,
        token: `mock-jwt-token-${mockUser.user.id}-${Date.now()}`,
        user: mockUser.user
      };
    }

    // Se não encontrou nas credenciais mock, tentar API real
    try {
      const response = await this.apiClient.post('/auth/login', {
        email,
        password,
      });
      return response.data;
    } catch (error) {
      console.error('Erro no login:', error);
      throw new Error('Email ou senha inválidos');
    }
  }

  async register(userData: {
    name: string;
    email: string;
    password: string;
  }): Promise<LoginResponse> {
    try {
      const response = await this.apiClient.post('/auth/register', userData);
      return response.data;
    } catch (error) {
      console.error('Erro no registro:', error);
      throw new Error('Falha no registro');
    }
  }

  async validateToken(token: string): Promise<User> {
    try {
      const response = await this.apiClient.get('/auth/validate', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      return response.data.user;
    } catch (error) {
      console.error('Erro na validação do token:', error);
      throw new Error('Token inválido');
    }
  }

  async refreshToken(): Promise<string> {
    try {
      const response = await this.apiClient.post('/auth/refresh');
      return response.data.token;
    } catch (error) {
      console.error('Erro ao renovar token:', error);
      throw new Error('Falha ao renovar token');
    }
  }

  async logout(): Promise<void> {
    try {
      await this.apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Erro no logout:', error);
    } finally {
      localStorage.removeItem('authToken');
    }
  }

  async changePassword(currentPassword: string, newPassword: string): Promise<boolean> {
    try {
      await this.apiClient.post('/auth/change-password', {
        currentPassword,
        newPassword,
      });
      return true;
    } catch (error) {
      console.error('Erro ao alterar senha:', error);
      return false;
    }
  }

  async resetPassword(email: string): Promise<boolean> {
    try {
      await this.apiClient.post('/auth/reset-password', { email });
      return true;
    } catch (error) {
      console.error('Erro ao solicitar reset de senha:', error);
      return false;
    }
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token;
  }
}

export const authService = new AuthService();
export default authService;