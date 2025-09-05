import React, { useState, useEffect } from 'react';
import { FileText, TrendingUp, TrendingDown, Clock, Eye, Star } from 'lucide-react';
import { apiService, AnalysisResult } from '../../services/apiService';
import LoadingSpinner from '../UI/LoadingSpinner';

type Analysis = AnalysisResult & {
  title?: string;
  type?: 'technical' | 'fundamental' | 'quantitative';
  createdAt?: string;
  summary?: string;
  author?: string;
  views?: number;
  rating?: number;
};

const RecentAnalyses: React.FC = () => {
  const [analyses, setAnalyses] = useState<Analysis[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadRecentAnalyses();
  }, []);

  const loadRecentAnalyses = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getRecentAnalyses();
      setAnalyses(data);
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar análises recentes:', err);
      setError('Erro ao carregar dados');
      // Dados mock para desenvolvimento
      setAnalyses([
        {
          id: '1',
          title: 'Análise Técnica PETR4 - Rompimento de Resistência',
          symbol: 'PETR4',
          analysis: 'PETR4 rompeu importante resistência em R$ 30,00 com volume expressivo. Próximo alvo em R$ 35,00.',
          type: 'technical',
          recommendation: 'BUY',
          confidence: 85,
          timestamp: '2024-01-15T10:30:00Z',
          agent: 'Agente Técnico',
          createdAt: '2024-01-15T10:30:00Z',
          summary: 'PETR4 rompeu importante resistência em R$ 30,00 com volume expressivo. Próximo alvo em R$ 35,00.',
          author: 'Agente Técnico',
          views: 1247,
          rating: 4.5,
        },
        {
          id: '2',
          title: 'Análise Fundamentalista VALE3 - Resultados Q4',
          symbol: 'VALE3',
          analysis: 'Resultados do Q4 dentro do esperado. Preço do minério de ferro permanece volátil.',
          type: 'fundamental',
          recommendation: 'HOLD',
          confidence: 72,
          timestamp: '2024-01-15T09:15:00Z',
          agent: 'Agente Fundamentalista',
          createdAt: '2024-01-15T09:15:00Z',
          summary: 'Resultados do Q4 dentro do esperado. Preço do minério de ferro permanece volátil.',
          author: 'Agente Fundamentalista',
          views: 892,
          rating: 4.2,
        },
        {
          id: '3',
          title: 'Modelo Quantitativo ITUB4 - Oportunidade de Valor',
          symbol: 'ITUB4',
          analysis: 'Modelo indica subavaliação de 15%. P/VPA atrativo comparado aos pares.',
          type: 'quantitative',
          recommendation: 'BUY',  
          confidence: 78,
          timestamp: '2024-01-15T08:45:00Z',
          agent: 'Agente Quantitativo',
          createdAt: '2024-01-15T08:45:00Z',
          summary: 'Modelo indica subavaliação de 15%. P/VPA atrativo comparado aos pares.',
          author: 'Agente Quantitativo',
          views: 634,
          rating: 4.0,
        },
        {
          id: '4',
          title: 'Análise Técnica BBDC4 - Padrão de Reversão',
          symbol: 'BBDC4',
          analysis: 'Formação de topo duplo sugere correção para R$ 13,50. Stop loss em R$ 16,20.',
          type: 'technical',
          recommendation: 'SELL',
          confidence: 68,
          timestamp: '2024-01-15T07:20:00Z',
          agent: 'Agente Técnico',
          createdAt: '2024-01-15T07:20:00Z',
          summary: 'Formação de topo duplo sugere correção para R$ 13,50. Stop loss em R$ 16,20.',
          author: 'Agente Técnico',
          views: 456,
          rating: 3.8,
        },
        {
          id: '5',
          title: 'Análise Setorial - Bancos em Foco',
          symbol: 'SETOR',
          analysis: 'Setor bancário apresenta fundamentos sólidos, mas enfrenta pressão regulatória.',
          type: 'fundamental',
          recommendation: 'HOLD', 
          confidence: 75,
          timestamp: '2024-01-14T16:30:00Z',
          agent: 'Agente Setorial',
          createdAt: '2024-01-14T16:30:00Z',
          summary: 'Setor bancário apresenta fundamentos sólidos, mas enfrenta pressão regulatória.',
          author: 'Agente Setorial',
          views: 1123,
          rating: 4.3,
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const getTypeIcon = (type: Analysis['type']) => {
    switch (type) {
      case 'technical':
        return <TrendingUp className="w-4 h-4" />;
      case 'fundamental':
        return <FileText className="w-4 h-4" />;
      case 'quantitative':
        return <TrendingDown className="w-4 h-4" />;
      default:
        return <FileText className="w-4 h-4" />;
    }
  };

  const getTypeLabel = (type: Analysis['type']) => {
    switch (type) {
      case 'technical':
        return 'Técnica';
      case 'fundamental':
        return 'Fundamentalista';
      case 'quantitative':
        return 'Quantitativa';
      default:
        return 'Análise';
    }
  };

  const getRecommendationColor = (recommendation: Analysis['recommendation']) => {
    switch (recommendation) {
      case 'BUY':
        return 'text-success-600 bg-success-100';
      case 'SELL':
        return 'text-danger-600 bg-danger-100';
      case 'HOLD':
        return 'text-warning-600 bg-warning-100';
      default:
        return 'text-secondary-600 bg-secondary-100';
    }
  };

  const getRecommendationLabel = (recommendation: Analysis['recommendation']) => {
    switch (recommendation) {
      case 'BUY':
        return 'Compra';
      case 'SELL':
        return 'Venda';
      case 'HOLD':
        return 'Manter';
      default:
        return 'Neutro';
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));
    
    if (diffInHours < 1) {
      return 'Agora mesmo';
    } else if (diffInHours < 24) {
      return `${diffInHours}h atrás`;
    } else {
      const diffInDays = Math.floor(diffInHours / 24);
      return `${diffInDays}d atrás`;
    }
  };

  if (isLoading) {
    return (
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Análises Recentes</h3>
        </div>
        <div className="card-content">
          <div className="flex items-center justify-center h-64">
            <LoadingSpinner size="lg" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">Análises Recentes</h3>
        <p className="card-description">
          Últimas análises geradas pelos agentes
        </p>
      </div>
      <div className="card-content">
        {error && (
          <div className="text-center py-4 mb-4">
            <p className="text-danger-600 text-sm mb-2">{error}</p>
            <button
              onClick={loadRecentAnalyses}
              className="btn-primary btn-sm"
            >
              Tentar Novamente
            </button>
          </div>
        )}
        
        <div className="space-y-4">
          {analyses.map((analysis) => (
            <div
              key={analysis.id}
              className="border border-secondary-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center space-x-2">
                  <div className="flex items-center space-x-1 text-primary-600">
                    {getTypeIcon(analysis.type)}
                    <span className="text-sm font-medium">
                      {getTypeLabel(analysis.type)}
                    </span>
                  </div>
                  <span className="text-secondary-400">•</span>
                  <span className="text-sm font-medium text-secondary-900">
                    {analysis.symbol}
                  </span>
                </div>
                
                <div className="flex items-center space-x-2">
                  <span className={`
                    px-2 py-1 text-xs font-medium rounded-full
                    ${getRecommendationColor(analysis.recommendation)}
                  `}>
                    {getRecommendationLabel(analysis.recommendation)}
                  </span>
                  <div className="text-xs text-secondary-500">
                    {analysis.confidence}%
                  </div>
                </div>
              </div>
              
              <h4 className="font-medium text-secondary-900 mb-2 line-clamp-2">
                {analysis.title}
              </h4>
              
              <p className="text-sm text-secondary-600 mb-3 line-clamp-2">
                {analysis.summary}
              </p>
              
              <div className="flex items-center justify-between text-xs text-secondary-500">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-1">
                    <Clock className="w-3 h-3" />
                    <span>{formatDate(analysis.createdAt || '')}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Eye className="w-3 h-3" />
                    <span>{analysis.views}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Star className="w-3 h-3" />
                    <span>{(analysis.rating || 0).toFixed(1)}</span>
                  </div>
                </div>
                
                <div className="text-secondary-600">
                  por {analysis.author}
                </div>
              </div>
            </div>
          ))}
        </div>
        
        <div className="mt-6 pt-4 border-t border-secondary-200">
          <div className="text-center">
            <button className="btn-outline btn-sm">
              Ver Todas as Análises
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecentAnalyses;