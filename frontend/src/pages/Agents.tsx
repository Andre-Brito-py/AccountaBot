import React, { useState, useEffect } from 'react';
import { Bot, Plus, Settings, Play, Pause, Trash2, Edit, Activity, Clock, CheckCircle, AlertCircle, XCircle, BarChart3, Brain, TrendingUp, MessageSquare, Shield } from 'lucide-react';
import { apiService, Agent as ApiAgent } from '../services/apiService';
import LoadingSpinner from '../components/UI/LoadingSpinner';

type Agent = ApiAgent & {
  version?: string;
  createdAt?: string;
  lastActivity?: string;
  tasksCompleted?: number;
  tasksInProgress?: number;
  successRate?: number;
  configuration?: {
    maxConcurrentTasks?: number;
    analysisFrequency?: string;
    symbols?: string[];
    parameters?: Record<string, any>;
  } & Record<string, any>;
  performance?: {
    avgExecutionTime: number;
    errorRate: number;
    uptime: number;
  };
};

interface AgentTemplate {
  id: string;
  name: string;
  type: Agent['type'];
  description: string;
  defaultConfig: Agent['configuration'];
}

const Agents: React.FC = () => {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [selectedAgent, setSelectedAgent] = useState<Agent | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [activeTab, setActiveTab] = useState<'overview' | 'performance' | 'logs'>('overview');

  const agentTemplates: AgentTemplate[] = [
    {
      id: 'technical',
      name: 'Agente Técnico',
      type: 'technical',
      description: 'Análise técnica e padrões gráficos',
      defaultConfig: {
        maxConcurrentTasks: 5,
        analysisFrequency: '15min',
        symbols: ['PETR4', 'VALE3', 'ITUB4'],
        parameters: {
          indicators: ['RSI', 'MACD', 'Bollinger Bands'],
          timeframes: ['1h', '4h', '1d'],
          sensitivity: 'medium'
        }
      }
    },
    {
      id: 'fundamental',
      name: 'Agente Fundamentalista',
      type: 'fundamental',
      description: 'Análise de fundamentos e balanços',
      defaultConfig: {
        maxConcurrentTasks: 3,
        analysisFrequency: '1h',
        symbols: ['PETR4', 'VALE3', 'ITUB4'],
        parameters: {
          metrics: ['P/E', 'P/VPA', 'ROE', 'ROIC'],
          sectors: ['financeiro', 'energia', 'mineração'],
          depth: 'detailed'
        }
      }
    },
    {
      id: 'quantitative',
      name: 'Agente Quantitativo',
      type: 'quantitative',
      description: 'Modelos matemáticos e estatísticos',
      defaultConfig: {
        maxConcurrentTasks: 10,
        analysisFrequency: '5min',
        symbols: ['PETR4', 'VALE3', 'ITUB4'],
        parameters: {
          models: ['mean_reversion', 'momentum', 'pairs_trading'],
          lookback: 252,
          confidence: 0.95
        }
      }
    },
    {
      id: 'sentiment',
      name: 'Agente de Sentimento',
      type: 'sentiment',
      description: 'Análise de sentimento e notícias',
      defaultConfig: {
        maxConcurrentTasks: 8,
        analysisFrequency: '30min',
        symbols: ['PETR4', 'VALE3', 'ITUB4'],
        parameters: {
          sources: ['news', 'social_media', 'analyst_reports'],
          languages: ['pt', 'en'],
          sentiment_threshold: 0.6
        }
      }
    },
    {
      id: 'risk',
      name: 'Agente de Risco',
      type: 'risk',
      description: 'Avaliação e gestão de riscos',
      defaultConfig: {
        maxConcurrentTasks: 5,
        analysisFrequency: '1h',
        symbols: ['PETR4', 'VALE3', 'ITUB4'],
        parameters: {
          risk_metrics: ['VaR', 'CVaR', 'Sharpe', 'Sortino'],
          confidence_levels: [0.95, 0.99],
          horizon: 21
        }
      }
    }
  ];

  useEffect(() => {
    loadAgents();
  }, []);

  const loadAgents = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getAgents();
      setAgents(data);
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar agentes:', err);
      setError('Erro ao carregar dados');
      // Dados mock para desenvolvimento
      const mockAgents: Agent[] = [
        {
          id: '1',
          name: 'Agente Técnico Principal',
          type: 'technical',
          status: 'ACTIVE',
          description: 'Análise técnica e padrões gráficos para ações brasileiras',
          version: '2.1.0',
          createdAt: '2024-01-01T00:00:00Z',
          lastActivity: '2024-01-15T10:30:00Z',
          tasksCompleted: 1247,
          tasksInProgress: 3,
          successRate: 94.2,
          configuration: {
            maxConcurrentTasks: 5,
            analysisFrequency: '15min',
            symbols: ['PETR4', 'VALE3', 'ITUB4', 'BBDC4', 'ABEV3'],
            parameters: {
              indicators: ['RSI', 'MACD', 'Bollinger Bands', 'Stochastic'],
              timeframes: ['1h', '4h', '1d'],
              sensitivity: 'high'
            }
          },
          performance: {
            avgExecutionTime: 2.3,
            errorRate: 0.8,
            uptime: 99.2
          }
        },
        {
          id: '2',
          name: 'Agente Fundamentalista',
          type: 'fundamental',
          status: 'ACTIVE',
          description: 'Análise de fundamentos empresariais e setoriais',
          version: '1.8.3',
          createdAt: '2024-01-01T00:00:00Z',
          lastActivity: '2024-01-15T10:25:00Z',
          tasksCompleted: 892,
          tasksInProgress: 2,
          successRate: 91.8,
          configuration: {
            maxConcurrentTasks: 3,
            analysisFrequency: '1h',
            symbols: ['PETR4', 'VALE3', 'ITUB4'],
            parameters: {
              metrics: ['P/E', 'P/VPA', 'ROE', 'ROIC', 'Dividend Yield'],
              sectors: ['financeiro', 'energia', 'mineração'],
              depth: 'detailed'
            }
          },
          performance: {
            avgExecutionTime: 15.7,
            errorRate: 1.2,
            uptime: 98.5
          }
        },
        {
          id: '3',
          name: 'Agente Quantitativo',
          type: 'quantitative',
          status: 'INACTIVE',
          description: 'Modelos matemáticos e estratégias quantitativas',
          version: '3.0.1',
          createdAt: '2024-01-01T00:00:00Z',
          lastActivity: '2024-01-15T09:45:00Z',
          tasksCompleted: 634,
          tasksInProgress: 0,
          successRate: 96.5,
          configuration: {
            maxConcurrentTasks: 10,
            analysisFrequency: '5min',
            symbols: ['PETR4', 'VALE3', 'ITUB4', 'BBDC4'],
            parameters: {
              models: ['mean_reversion', 'momentum', 'pairs_trading'],
              lookback: 252,
              confidence: 0.95
            }
          },
          performance: {
            avgExecutionTime: 0.8,
            errorRate: 0.3,
            uptime: 99.8
          }
        }
      ];
      setAgents(mockAgents);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusIcon = (status: Agent['status']) => {
    switch (status) {
      case 'ACTIVE':
        return <CheckCircle className="w-3 h-3 text-success-600" />;
      case 'INACTIVE':
        return <Clock className="w-3 h-3 text-warning-600" />;
      case 'RUNNING':
        return <Activity className="w-3 h-3 text-primary-600" />;
      default:
        return <Clock className="w-3 h-3 text-secondary-600" />;
    }
  };

  const getStatusColor = (status: Agent['status']) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-success-100 text-success-800 border-success-200';
      case 'INACTIVE':
        return 'bg-warning-100 text-warning-800 border-warning-200';
      case 'RUNNING':
        return 'bg-primary-100 text-primary-800 border-primary-200';
      default:
        return 'bg-secondary-100 text-secondary-800 border-secondary-200';
    }
  };

  const getTypeIcon = (type: Agent['type']) => {
    switch (type) {
      case 'technical':
        return <TrendingUp className="w-6 h-6" />;
      case 'fundamental':
        return <BarChart3 className="w-6 h-6" />;
      case 'quantitative':
        return <Brain className="w-6 h-6" />;
      case 'sentiment':
        return <MessageSquare className="w-6 h-6" />;
      case 'risk':
        return <Shield className="w-6 h-6" />;
      default:
        return <Bot className="w-6 h-6" />;
    }
  };

  const handleAgentAction = async (agentId: string, action: 'start' | 'stop' | 'restart' | 'delete') => {
    try {
      switch (action) {
        case 'start':
          await apiService.startAgent(agentId);
          break;
        case 'stop':
          await apiService.stopAgent(agentId);
          break;
        case 'restart':
          await apiService.restartAgent(agentId);
          break;
        case 'delete':
          if (window.confirm('Tem certeza que deseja excluir este agente?')) {
            await apiService.deleteAgent(agentId);
          }
          break;
      }
      loadAgents();
    } catch (err) {
      console.error(`Erro ao executar ação ${action}:`, err);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-secondary-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-center h-64">
            <LoadingSpinner size="lg" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-secondary-50 p-6">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-secondary-900 mb-2">Agentes</h1>
            <p className="text-secondary-600">
              Gerencie seus agentes de IA para análise financeira
            </p>
          </div>
          <button
            onClick={() => setShowCreateModal(true)}
            className="btn-primary"
          >
            <Plus className="w-4 h-4 mr-2" />
            Novo Agente
          </button>
        </div>

        {error && (
          <div className="card mb-6">
            <div className="card-content">
              <div className="text-center py-4">
                <p className="text-danger-600 text-sm mb-2">{error}</p>
                <button
                  onClick={loadAgents}
                  className="btn-primary btn-sm"
                >
                  Tentar Novamente
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Resumo */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="card">
            <div className="card-content text-center">
              <div className="text-3xl font-bold text-primary-600 mb-2">
                {agents.length}
              </div>
              <div className="text-sm text-secondary-600">Total de Agentes</div>
            </div>
          </div>
          <div className="card">
            <div className="card-content text-center">
              <div className="text-3xl font-bold text-success-600 mb-2">
                {agents.filter(a => a.status === 'ACTIVE' || a.status === 'RUNNING').length}
              </div>
              <div className="text-sm text-secondary-600">Agentes Ativos</div>
            </div>
          </div>
          <div className="card">
            <div className="card-content text-center">
              <div className="text-3xl font-bold text-warning-600 mb-2">
                {agents.reduce((sum, a) => sum + (a.tasksInProgress || 0), 0)}
              </div>
              <div className="text-sm text-secondary-600">Tarefas em Andamento</div>
            </div>
          </div>
          <div className="card">
            <div className="card-content text-center">
              <div className="text-3xl font-bold text-secondary-900 mb-2">
                {(agents.reduce((sum, a) => sum + (a.successRate || 0), 0) / agents.length || 0).toFixed(1)}%
              </div>
              <div className="text-sm text-secondary-600">Taxa de Sucesso Média</div>
            </div>
          </div>
        </div>

        {/* Lista de Agentes */}
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
          {agents.map((agent) => (
            <div key={agent.id} className="card hover:shadow-lg transition-shadow">
              <div className="card-content">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-primary-100 rounded-lg text-primary-600">
                      {getTypeIcon(agent.type)}
                    </div>
                    <div>
                      <h3 className="font-semibold text-secondary-900">
                        {agent.name}
                      </h3>
                      <p className="text-sm text-secondary-600">
                        v{agent.version}
                      </p>
                    </div>
                  </div>
                  
                  <div className={`
                    px-2 py-1 rounded-full border flex items-center space-x-1
                    ${getStatusColor(agent.status)}
                  `}>
                    {getStatusIcon(agent.status)}
                    <span className="text-xs font-medium capitalize">
                      {agent.status === 'ACTIVE' ? 'Ativo' :
                       agent.status === 'INACTIVE' ? 'Inativo' :
                       agent.status === 'RUNNING' ? 'Executando' : 'Desconhecido'}
                    </span>
                  </div>
                </div>
                
                <p className="text-sm text-secondary-600 mb-4">
                  {agent.description}
                </p>
                
                <div className="grid grid-cols-3 gap-4 mb-4">
                  <div className="text-center">
                    <div className="text-lg font-semibold text-secondary-900">
                      {agent.tasksCompleted || 0}
                    </div>
                    <div className="text-xs text-secondary-600">Concluídas</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-semibold text-warning-600">
                      {agent.tasksInProgress || 0}
                    </div>
                    <div className="text-xs text-secondary-600">Em Andamento</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-semibold text-success-600">
                      {agent.successRate?.toFixed(1) || '0.0'}%
                    </div>
                    <div className="text-xs text-secondary-600">Sucesso</div>
                  </div>
                </div>
                
                <div className="text-xs text-secondary-500 mb-4">
                  Última atividade: {formatDate(agent.lastActivity || '')}
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleAgentAction(agent.id, (agent.status === 'ACTIVE' || agent.status === 'RUNNING') ? 'stop' : 'start')}
                      className={`
                        p-2 rounded-lg transition-colors
                        ${(agent.status === 'ACTIVE' || agent.status === 'RUNNING') 
                          ? 'bg-warning-100 text-warning-600 hover:bg-warning-200'
                          : 'bg-success-100 text-success-600 hover:bg-success-200'
                        }
                      `}
                      title={(agent.status === 'ACTIVE' || agent.status === 'RUNNING') ? 'Pausar' : 'Iniciar'}
                    >
                      {(agent.status === 'ACTIVE' || agent.status === 'RUNNING') ? (
                        <Pause className="w-4 h-4" />
                      ) : (
                        <Play className="w-4 h-4" />
                      )}
                    </button>
                    
                    <button
                      onClick={() => handleAgentAction(agent.id, 'restart')}
                      className="p-2 bg-secondary-100 text-secondary-600 hover:bg-secondary-200 rounded-lg transition-colors"
                      title="Reiniciar"
                    >
                      <Activity className="w-4 h-4" />
                    </button>
                    
                    <button
                      onClick={() => {
                        setSelectedAgent(agent);
                        setShowConfigModal(true);
                      }}
                      className="p-2 bg-primary-100 text-primary-600 hover:bg-primary-200 rounded-lg transition-colors"
                      title="Configurar"
                    >
                      <Settings className="w-4 h-4" />
                    </button>
                  </div>
                  
                  <button
                    onClick={() => setSelectedAgent(agent)}
                    className="btn-outline btn-sm"
                  >
                    Ver Detalhes
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {agents.length === 0 && !isLoading && (
          <div className="card">
            <div className="card-content">
              <div className="text-center py-12">
                <Bot className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-secondary-900 mb-2">
                  Nenhum agente encontrado
                </h3>
                <p className="text-secondary-600 mb-4">
                  Crie seu primeiro agente para começar a análise automatizada
                </p>
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="btn-primary"
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Criar Agente
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Modal de Detalhes do Agente */}
      {selectedAgent && !showConfigModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex items-start justify-between mb-6">
                <div className="flex items-center space-x-4">
                  <div className="p-3 bg-primary-100 rounded-lg text-primary-600">
                    {getTypeIcon(selectedAgent.type)}
                  </div>
                  <div>
                    <h2 className="text-2xl font-bold text-secondary-900">
                      {selectedAgent.name}
                    </h2>
                    <p className="text-secondary-600">
                      {selectedAgent.description}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedAgent(null)}
                  className="text-secondary-400 hover:text-secondary-600"
                >
                  ✕
                </button>
              </div>
              
              {/* Tabs */}
              <div className="flex space-x-1 mb-6 bg-secondary-100 p-1 rounded-lg">
                {['overview', 'performance', 'logs'].map((tab) => (
                  <button
                    key={tab}
                    className={`
                      flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors capitalize
                      ${
                        activeTab === tab
                          ? 'bg-white text-secondary-900 shadow-sm'
                          : 'text-secondary-600 hover:text-secondary-900'
                      }
                    `}
                    onClick={() => setActiveTab(tab as any)}
                  >
                    {tab === 'overview' ? 'Visão Geral' :
                     tab === 'performance' ? 'Performance' : 'Logs'}
                  </button>
                ))}
              </div>
              
              {/* Conteúdo das Tabs */}
              {activeTab === 'overview' && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="card">
                      <div className="card-header">
                        <h3 className="card-title">Informações Gerais</h3>
                      </div>
                      <div className="card-content space-y-3">
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Versão:</span>
                          <span className="font-medium">v{selectedAgent.version}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Criado em:</span>
                          <span className="font-medium">{formatDate(selectedAgent.createdAt || '')}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Última atividade:</span>
                          <span className="font-medium">{formatDate(selectedAgent.lastActivity || '')}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Status:</span>
                          <div className={`
                            px-2 py-1 rounded-full text-xs font-medium flex items-center space-x-1
                            ${getStatusColor(selectedAgent.status)}
                          `}>
                            {getStatusIcon(selectedAgent.status)}
                            <span className="capitalize">{selectedAgent.status}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <div className="card">
                      <div className="card-header">
                        <h3 className="card-title">Estatísticas</h3>
                      </div>
                      <div className="card-content space-y-3">
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Tarefas concluídas:</span>
                          <span className="font-medium text-success-600">{selectedAgent.tasksCompleted || 0}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Em andamento:</span>
                          <span className="font-medium text-warning-600">{selectedAgent.tasksInProgress || 0}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Taxa de sucesso:</span>
                          <span className="font-medium text-primary-600">{selectedAgent.successRate?.toFixed(1) || '0.0'}%</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="card">
                    <div className="card-header">
                      <h3 className="card-title">Configuração</h3>
                    </div>
                    <div className="card-content">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label className="text-sm font-medium text-secondary-700">Tarefas Simultâneas:</label>
                          <p className="text-secondary-900">{selectedAgent.configuration?.maxConcurrentTasks || 0}</p>
                        </div>
                        <div>
                          <label className="text-sm font-medium text-secondary-700">Frequência de Análise:</label>
                          <p className="text-secondary-900">{selectedAgent.configuration?.analysisFrequency || 'N/A'}</p>
                        </div>
                        <div className="md:col-span-2">
                          <label className="text-sm font-medium text-secondary-700">Símbolos Monitorados:</label>
                          <div className="flex flex-wrap gap-2 mt-1">
                            {(selectedAgent.configuration?.symbols || []).map((symbol) => (
                              <span
                                key={symbol}
                                className="px-2 py-1 bg-primary-100 text-primary-700 rounded text-sm"
                              >
                                {symbol}
                              </span>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              
              {activeTab === 'performance' && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="card">
                      <div className="card-content text-center">
                        <div className="text-2xl font-bold text-primary-600 mb-2">
                          {selectedAgent.performance?.avgExecutionTime?.toFixed(1) || '0.0'}s
                        </div>
                        <div className="text-sm text-secondary-600">Tempo Médio de Execução</div>
                      </div>
                    </div>
                    <div className="card">
                      <div className="card-content text-center">
                        <div className="text-2xl font-bold text-danger-600 mb-2">
                          {selectedAgent.performance?.errorRate?.toFixed(1) || '0.0'}%
                        </div>
                        <div className="text-sm text-secondary-600">Taxa de Erro</div>
                      </div>
                    </div>
                    <div className="card">
                      <div className="card-content text-center">
                        <div className="text-2xl font-bold text-success-600 mb-2">
                          {selectedAgent.performance?.uptime?.toFixed(1) || '0.0'}%
                        </div>
                        <div className="text-sm text-secondary-600">Uptime</div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              
              {activeTab === 'logs' && (
                <div className="card">
                  <div className="card-content">
                    <div className="text-center py-12">
                      <Activity className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-secondary-900 mb-2">
                        Logs do Agente
                      </h3>
                      <p className="text-secondary-600">
                        Funcionalidade em desenvolvimento
                      </p>
                    </div>
                  </div>
                </div>
              )}
              
              <div className="flex justify-end space-x-3 mt-6 pt-6 border-t border-secondary-200">
                <button
                  onClick={() => {
                    setShowConfigModal(true);
                  }}
                  className="btn-outline"
                >
                  <Settings className="w-4 h-4 mr-2" />
                  Configurar
                </button>
                <button
                  onClick={() => handleAgentAction(selectedAgent.id, 'delete')}
                  className="btn-danger"
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Excluir
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Criação de Agente */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-secondary-900">
                  Criar Novo Agente
                </h2>
                <button
                  onClick={() => setShowCreateModal(false)}
                  className="text-secondary-400 hover:text-secondary-600"
                >
                  ✕
                </button>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {agentTemplates.map((template) => (
                  <div
                    key={template.id}
                    className="border border-secondary-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
                    onClick={() => {
                      // Implementar criação de agente
                      console.log('Criar agente:', template);
                      setShowCreateModal(false);
                    }}
                  >
                    <div className="flex items-center space-x-3 mb-3">
                      <div className="p-2 bg-primary-100 rounded-lg text-primary-600">
                        {getTypeIcon(template.type)}
                      </div>
                      <div>
                        <h3 className="font-semibold text-secondary-900">
                          {template.name}
                        </h3>
                      </div>
                    </div>
                    <p className="text-sm text-secondary-600">
                      {template.description}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Configuração */}
      {showConfigModal && selectedAgent && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-secondary-900">
                  Configurar {selectedAgent.name}
                </h2>
                <button
                  onClick={() => setShowConfigModal(false)}
                  className="text-secondary-400 hover:text-secondary-600"
                >
                  ✕
                </button>
              </div>
              
              <div className="text-center py-12">
                <Settings className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-secondary-900 mb-2">
                  Configuração do Agente
                </h3>
                <p className="text-secondary-600">
                  Funcionalidade em desenvolvimento
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Agents;