import React, { useState, useEffect } from 'react';
import { Bot, Activity, Clock, CheckCircle, AlertCircle, XCircle, Play, Pause, Settings } from 'lucide-react';
import { apiService } from '../../services/apiService';
import LoadingSpinner from '../UI/LoadingSpinner';

interface Agent {
  id: string;
  name: string;
  type: 'technical' | 'fundamental' | 'quantitative' | 'sentiment' | 'risk';
  status: 'active' | 'idle' | 'error' | 'maintenance';
  lastActivity: string;
  tasksCompleted: number;
  tasksInProgress: number;
  successRate: number;
  description: string;
  version: string;
}

const AgentStatus: React.FC = () => {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadAgentStatus();
    // Atualizar status a cada 30 segundos
    const interval = setInterval(loadAgentStatus, 30000);
    return () => clearInterval(interval);
  }, []);

  const loadAgentStatus = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getAgentStatus();
      setAgents(data);
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar status dos agentes:', err);
      setError('Erro ao carregar dados');
      // Dados mock para desenvolvimento
      setAgents([
        {
          id: '1',
          name: 'Agente Técnico',
          type: 'technical',
          status: 'active',
          lastActivity: '2024-01-15T10:30:00Z',
          tasksCompleted: 45,
          tasksInProgress: 3,
          successRate: 94.2,
          description: 'Análise técnica e padrões gráficos',
          version: '2.1.0',
        },
        {
          id: '2',
          name: 'Agente Fundamentalista',
          type: 'fundamental',
          status: 'active',
          lastActivity: '2024-01-15T10:25:00Z',
          tasksCompleted: 32,
          tasksInProgress: 2,
          successRate: 91.8,
          description: 'Análise de fundamentos e balanços',
          version: '1.8.3',
        },
        {
          id: '3',
          name: 'Agente Quantitativo',
          type: 'quantitative',
          status: 'idle',
          lastActivity: '2024-01-15T09:45:00Z',
          tasksCompleted: 28,
          tasksInProgress: 0,
          successRate: 96.5,
          description: 'Modelos matemáticos e estatísticos',
          version: '3.0.1',
        },
        {
          id: '4',
          name: 'Agente de Sentimento',
          type: 'sentiment',
          status: 'active',
          lastActivity: '2024-01-15T10:28:00Z',
          tasksCompleted: 67,
          tasksInProgress: 5,
          successRate: 88.3,
          description: 'Análise de sentimento e notícias',
          version: '1.5.2',
        },
        {
          id: '5',
          name: 'Agente de Risco',
          type: 'risk',
          status: 'error',
          lastActivity: '2024-01-15T08:15:00Z',
          tasksCompleted: 19,
          tasksInProgress: 0,
          successRate: 92.1,
          description: 'Avaliação e gestão de riscos',
          version: '2.0.0',
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusIcon = (status: Agent['status']) => {
    switch (status) {
      case 'active':
        return <CheckCircle className="w-4 h-4 text-success-600" />;
      case 'idle':
        return <Clock className="w-4 h-4 text-warning-600" />;
      case 'error':
        return <XCircle className="w-4 h-4 text-danger-600" />;
      case 'maintenance':
        return <AlertCircle className="w-4 h-4 text-secondary-600" />;
      default:
        return <Clock className="w-4 h-4 text-secondary-600" />;
    }
  };

  const getStatusLabel = (status: Agent['status']) => {
    switch (status) {
      case 'active':
        return 'Ativo';
      case 'idle':
        return 'Inativo';
      case 'error':
        return 'Erro';
      case 'maintenance':
        return 'Manutenção';
      default:
        return 'Desconhecido';
    }
  };

  const getStatusColor = (status: Agent['status']) => {
    switch (status) {
      case 'active':
        return 'bg-success-100 text-success-800';
      case 'idle':
        return 'bg-warning-100 text-warning-800';
      case 'error':
        return 'bg-danger-100 text-danger-800';
      case 'maintenance':
        return 'bg-secondary-100 text-secondary-800';
      default:
        return 'bg-secondary-100 text-secondary-800';
    }
  };

  const getTypeIcon = (type: Agent['type']) => {
    switch (type) {
      case 'technical':
        return '📈';
      case 'fundamental':
        return '📊';
      case 'quantitative':
        return '🔢';
      case 'sentiment':
        return '💭';
      case 'risk':
        return '⚠️';
      default:
        return '🤖';
    }
  };

  const formatLastActivity = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
    
    if (diffInMinutes < 1) {
      return 'Agora mesmo';
    } else if (diffInMinutes < 60) {
      return `${diffInMinutes}min atrás`;
    } else {
      const diffInHours = Math.floor(diffInMinutes / 60);
      if (diffInHours < 24) {
        return `${diffInHours}h atrás`;
      } else {
        const diffInDays = Math.floor(diffInHours / 24);
        return `${diffInDays}d atrás`;
      }
    }
  };

  const handleAgentAction = (agentId: string, action: 'start' | 'stop' | 'restart') => {
    console.log(`Ação ${action} no agente ${agentId}`);
    // Implementar ações do agente
  };

  if (isLoading) {
    return (
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Status dos Agentes</h3>
        </div>
        <div className="card-content">
          <div className="flex items-center justify-center h-64">
            <LoadingSpinner size="lg" />
          </div>
        </div>
      </div>
    );
  }

  const activeAgents = agents.filter(agent => agent.status === 'active').length;
  const totalTasks = agents.reduce((sum, agent) => sum + agent.tasksInProgress, 0);
  const avgSuccessRate = agents.reduce((sum, agent) => sum + agent.successRate, 0) / agents.length;

  return (
    <div className="card">
      <div className="card-header">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="card-title">Status dos Agentes</h3>
            <p className="card-description">
              Monitoramento em tempo real
            </p>
          </div>
          <div className="flex items-center space-x-2">
            <div className="flex items-center space-x-1 text-sm text-success-600">
              <Activity className="w-4 h-4" />
              <span>{activeAgents}/{agents.length} ativos</span>
            </div>
          </div>
        </div>
      </div>
      <div className="card-content">
        {error && (
          <div className="text-center py-4 mb-4">
            <p className="text-danger-600 text-sm mb-2">{error}</p>
            <button
              onClick={loadAgentStatus}
              className="btn-primary btn-sm"
            >
              Tentar Novamente
            </button>
          </div>
        )}
        
        {/* Resumo */}
        <div className="grid grid-cols-3 gap-4 mb-6">
          <div className="text-center p-3 bg-primary-50 rounded-lg">
            <div className="text-2xl font-bold text-primary-600">{activeAgents}</div>
            <div className="text-sm text-primary-700">Agentes Ativos</div>
          </div>
          <div className="text-center p-3 bg-warning-50 rounded-lg">
            <div className="text-2xl font-bold text-warning-600">{totalTasks}</div>
            <div className="text-sm text-warning-700">Tarefas em Andamento</div>
          </div>
          <div className="text-center p-3 bg-success-50 rounded-lg">
            <div className="text-2xl font-bold text-success-600">{avgSuccessRate.toFixed(1)}%</div>
            <div className="text-sm text-success-700">Taxa de Sucesso</div>
          </div>
        </div>
        
        {/* Lista de agentes */}
        <div className="space-y-3">
          {agents.map((agent) => (
            <div
              key={agent.id}
              className="border border-secondary-200 rounded-lg p-4 hover:shadow-sm transition-shadow"
            >
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-3">
                  <div className="text-2xl">{getTypeIcon(agent.type)}</div>
                  <div>
                    <div className="font-medium text-secondary-900">
                      {agent.name}
                    </div>
                    <div className="text-sm text-secondary-600">
                      {agent.description}
                    </div>
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  <span className={`
                    px-2 py-1 text-xs font-medium rounded-full flex items-center space-x-1
                    ${getStatusColor(agent.status)}
                  `}>
                    {getStatusIcon(agent.status)}
                    <span>{getStatusLabel(agent.status)}</span>
                  </span>
                </div>
              </div>
              
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-3">
                <div className="text-center">
                  <div className="text-lg font-semibold text-secondary-900">
                    {agent.tasksCompleted}
                  </div>
                  <div className="text-xs text-secondary-600">Concluídas</div>
                </div>
                <div className="text-center">
                  <div className="text-lg font-semibold text-warning-600">
                    {agent.tasksInProgress}
                  </div>
                  <div className="text-xs text-secondary-600">Em Andamento</div>
                </div>
                <div className="text-center">
                  <div className="text-lg font-semibold text-success-600">
                    {agent.successRate.toFixed(1)}%
                  </div>
                  <div className="text-xs text-secondary-600">Sucesso</div>
                </div>
                <div className="text-center">
                  <div className="text-lg font-semibold text-secondary-900">
                    v{agent.version}
                  </div>
                  <div className="text-xs text-secondary-600">Versão</div>
                </div>
              </div>
              
              <div className="flex items-center justify-between text-xs text-secondary-500">
                <div className="flex items-center space-x-1">
                  <Clock className="w-3 h-3" />
                  <span>Última atividade: {formatLastActivity(agent.lastActivity)}</span>
                </div>
                
                <div className="flex items-center space-x-1">
                  <button
                    onClick={() => handleAgentAction(agent.id, agent.status === 'active' ? 'stop' : 'start')}
                    className="p-1 hover:bg-secondary-100 rounded transition-colors"
                    title={agent.status === 'active' ? 'Pausar' : 'Iniciar'}
                  >
                    {agent.status === 'active' ? (
                      <Pause className="w-3 h-3" />
                    ) : (
                      <Play className="w-3 h-3" />
                    )}
                  </button>
                  <button
                    onClick={() => handleAgentAction(agent.id, 'restart')}
                    className="p-1 hover:bg-secondary-100 rounded transition-colors"
                    title="Reiniciar"
                  >
                    <Activity className="w-3 h-3" />
                  </button>
                  <button
                    className="p-1 hover:bg-secondary-100 rounded transition-colors"
                    title="Configurações"
                  >
                    <Settings className="w-3 h-3" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
        
        <div className="mt-6 pt-4 border-t border-secondary-200">
          <div className="text-center">
            <button className="btn-outline btn-sm">
              Gerenciar Agentes
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AgentStatus;