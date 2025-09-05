import axios, { AxiosInstance, AxiosResponse } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

interface FinancialData {
  symbol: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  marketCap?: number;
  timestamp: string;
}

interface AnalysisResult {
  id: string;
  symbol: string;
  analysis: string;
  recommendation: 'BUY' | 'SELL' | 'HOLD';
  confidence: number;
  timestamp: string;
  agent: string;
}

interface Agent {
  id: string;
  name: string;
  type: string;
  status: 'ACTIVE' | 'INACTIVE' | 'RUNNING';
  description: string;
  configuration: Record<string, any>;
  lastExecution?: string;
}

interface Portfolio {
  id: string;
  name: string;
  totalValue: number;
  totalCost: number;
  totalGainLoss: number;
  totalGainLossPercent: number;
  positions: Position[];
  createdAt: string;
  lastUpdate: string;
}

interface Position {
  id: string;
  symbol: string;
  name: string;
  quantity: number;
  avgPrice: number;
  currentPrice: number;
  totalValue: number;
  totalCost: number;
  gainLoss: number;
  gainLossPercent: number;
  sector: string;
  weight: number;
  lastUpdate: string;
}

class ApiService {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Interceptor para adicionar token de autenticação
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Interceptor para lidar com erros de resposta
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('authToken');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Métodos para dados financeiros
  async getMarketData(symbols: string[]): Promise<FinancialData[]> {
    const response = await this.client.get('/market/data', {
      params: { symbols: symbols.join(',') },
    });
    return response.data;
  }

  async getHistoricalData(
    symbol: string,
    period: string = '1y'
  ): Promise<any[]> {
    const response = await this.client.get(`/market/historical/${symbol}`, {
      params: { period },
    });
    return response.data;
  }

  async searchSymbols(query: string): Promise<any[]> {
    const response = await this.client.get('/market/search', {
      params: { q: query },
    });
    return response.data;
  }

  // Métodos para análises
  async getAnalyses(symbol?: string): Promise<AnalysisResult[]> {
    const response = await this.client.get('/analyses', {
      params: symbol ? { symbol } : {},
    });
    return response.data;
  }

  async createAnalysis(symbol: string, agentId: string): Promise<AnalysisResult> {
    const response = await this.client.post('/analyses', {
      symbol,
      agentId,
    });
    return response.data;
  }

  async getAnalysisById(id: string): Promise<AnalysisResult> {
    const response = await this.client.get(`/analyses/${id}`);
    return response.data;
  }

  // Métodos para agentes
  async getAgents(): Promise<Agent[]> {
    const response = await this.client.get('/agents');
    return response.data;
  }

  async getAgentById(id: string): Promise<Agent> {
    const response = await this.client.get(`/agents/${id}`);
    return response.data;
  }

  async createAgent(agent: Omit<Agent, 'id'>): Promise<Agent> {
    const response = await this.client.post('/agents', agent);
    return response.data;
  }

  async updateAgent(id: string, agent: Partial<Agent>): Promise<Agent> {
    const response = await this.client.put(`/agents/${id}`, agent);
    return response.data;
  }

  async deleteAgent(id: string): Promise<void> {
    await this.client.delete(`/agents/${id}`);
  }

  async executeAgent(id: string, parameters?: Record<string, any>): Promise<any> {
    const response = await this.client.post(`/agents/${id}/execute`, parameters);
    return response.data;
  }

  async startAgent(id: string): Promise<void> {
    await this.client.post(`/agents/${id}/start`);
  }

  async stopAgent(id: string): Promise<void> {
    await this.client.post(`/agents/${id}/stop`);
  }

  async restartAgent(id: string): Promise<void> {
    await this.client.post(`/agents/${id}/restart`);
  }

  async getAgentStatus(): Promise<any[]> {
    const response = await this.client.get('/agents/status');
    return response.data;
  }

  async getRecentAnalyses(): Promise<AnalysisResult[]> {
    const response = await this.client.get('/analyses/recent');
    return response.data;
  }

  // Métodos para portfólio
  async getPortfolios(): Promise<Portfolio[]> {
    const response = await this.client.get('/portfolios');
    return response.data;
  }

  async getPortfolioById(id: string): Promise<Portfolio> {
    const response = await this.client.get(`/portfolios/${id}`);
    return response.data;
  }

  async createPortfolio(portfolio: Omit<Portfolio, 'id' | 'totalValue' | 'totalReturn' | 'totalReturnPercent' | 'positions'>): Promise<Portfolio> {
    const response = await this.client.post('/portfolios', portfolio);
    return response.data;
  }

  async updatePortfolio(id: string, portfolio: Partial<Portfolio>): Promise<Portfolio> {
    const response = await this.client.put(`/portfolios/${id}`, portfolio);
    return response.data;
  }

  async deletePortfolio(id: string): Promise<void> {
    await this.client.delete(`/portfolios/${id}`);
  }

  async addPosition(portfolioId: string, position: Omit<Position, 'currentPrice' | 'totalValue' | 'unrealizedPnL' | 'unrealizedPnLPercent'>): Promise<Position> {
    const response = await this.client.post(`/portfolios/${portfolioId}/positions`, position);
    return response.data;
  }

  async updatePosition(portfolioId: string, symbol: string, position: Partial<Position>): Promise<Position> {
    const response = await this.client.put(`/portfolios/${portfolioId}/positions/${symbol}`, position);
    return response.data;
  }

  async removePosition(portfolioId: string, symbol: string): Promise<void> {
    await this.client.delete(`/portfolios/${portfolioId}/positions/${symbol}`);
  }

  // Métodos para dashboard
  async getDashboardData(): Promise<any> {
    const response = await this.client.get('/dashboard');
    return response.data;
  }

  async getMarketOverview(): Promise<any> {
    const response = await this.client.get('/dashboard/market-overview');
    return response.data;
  }

  async getTopMovers(): Promise<any> {
    const response = await this.client.get('/dashboard/top-movers');
    return response.data;
  }

  // Métodos para configurações
  async getUserSettings(): Promise<any> {
    const response = await this.client.get('/settings');
    return response.data;
  }

  async updateUserSettings(settings: any): Promise<any> {
    const response = await this.client.put('/settings', settings);
    return response.data;
  }

  // Método genérico para requisições customizadas
  async request<T = any>(
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    url: string,
    data?: any,
    config?: any
  ): Promise<AxiosResponse<T>> {
    return this.client.request({
      method,
      url,
      data,
      ...config,
    });
  }
}

export const apiService = new ApiService();
export default apiService;
export type { FinancialData, AnalysisResult, Agent, Portfolio, Position };