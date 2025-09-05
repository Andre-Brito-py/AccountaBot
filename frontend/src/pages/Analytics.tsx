import React, { useState, useEffect } from 'react';
import { Search, Filter, TrendingUp, TrendingDown, FileText, Calendar, Star, Eye, Download } from 'lucide-react';
import { apiService, AnalysisResult } from '../services/apiService';
import LoadingSpinner from '../components/UI/LoadingSpinner';
import StockChart from '../components/Charts/StockChart';

// Interface AnalysisResult agora importada do apiService
// Criando um alias para compatibilidade
type Analysis = AnalysisResult & {
  title?: string;
  type?: 'technical' | 'fundamental' | 'quantitative' | 'sentiment';
  createdAt?: string;
  summary?: string;
  content?: string;
  author?: string;
  views?: number;
  rating?: number;
  tags?: string[];
  targetPrice?: number;
  stopLoss?: number;
};

const Analytics: React.FC = () => {
  const [analyses, setAnalyses] = useState<Analysis[]>([]);
  const [filteredAnalyses, setFilteredAnalyses] = useState<Analysis[]>([]);
  const [selectedAnalysis, setSelectedAnalysis] = useState<Analysis | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<string>('all');
  const [selectedRecommendation, setSelectedRecommendation] = useState<string>('all');
  const [sortBy, setSortBy] = useState<'date' | 'confidence' | 'rating'>('date');

  useEffect(() => {
    loadAnalyses();
  }, []);

  useEffect(() => {
    filterAnalyses();
  }, [analyses, searchTerm, selectedType, selectedRecommendation, sortBy]);

  const loadAnalyses = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getAnalyses();
      setAnalyses(data);
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar análises:', err);
      setError('Erro ao carregar dados');
      // Dados mock para desenvolvimento
      const mockAnalyses: Analysis[] = [
        {
          id: '1',
          title: 'Análise Técnica PETR4 - Rompimento de Resistência Importante',
          symbol: 'PETR4',
          type: 'technical',
          recommendation: 'BUY',
          confidence: 85,
          createdAt: '2024-01-15T10:30:00Z',
          timestamp: '2024-01-15T10:30:00Z',
          analysis: 'PETR4 rompeu importante resistência em R$ 30,00 com volume expressivo. Próximo alvo em R$ 35,00.',
          agent: 'Agente Técnico',
          summary: 'PETR4 rompeu importante resistência em R$ 30,00 com volume expressivo. Próximo alvo em R$ 35,00.',
          content: 'Análise detalhada da ação PETR4 mostra rompimento de resistência técnica importante...',
          author: 'Agente Técnico',
          views: 1247,
          rating: 4.5,
          tags: ['petróleo', 'energia', 'rompimento', 'alta'],
          targetPrice: 35.00,
          stopLoss: 28.50,
        },
        {
          id: '2',
          title: 'Análise Fundamentalista VALE3 - Resultados Q4 e Perspectivas',
          symbol: 'VALE3',
          type: 'fundamental',
          recommendation: 'HOLD',
          confidence: 72,
          createdAt: '2024-01-15T09:15:00Z',
          timestamp: '2024-01-15T09:15:00Z',
          analysis: 'Resultados do Q4 dentro do esperado. Preço do minério de ferro permanece volátil.',
          agent: 'Agente Fundamentalista',
          summary: 'Resultados do Q4 dentro do esperado. Preço do minério de ferro permanece volátil.',
          content: 'A Vale apresentou resultados do Q4 alinhados com as expectativas do mercado...',
          author: 'Agente Fundamentalista',
          views: 892,
          rating: 4.2,
          tags: ['mineração', 'commodities', 'resultados', 'dividendos'],
          targetPrice: 75.00,
        },
        {
          id: '3',
          title: 'Modelo Quantitativo ITUB4 - Oportunidade de Valor Identificada',
          symbol: 'ITUB4',
          type: 'quantitative',
          recommendation: 'BUY',
          confidence: 78,
          createdAt: '2024-01-15T08:45:00Z',
          timestamp: '2024-01-15T08:45:00Z',
          analysis: 'Modelo indica subavaliação de 15%. P/VPA atrativo comparado aos pares.',
          agent: 'Agente Quantitativo',
          summary: 'Modelo indica subavaliação de 15%. P/VPA atrativo comparado aos pares.',
          content: 'Nosso modelo quantitativo identificou uma oportunidade de valor no ITUB4...',
          author: 'Agente Quantitativo',
          views: 634,
          rating: 4.0,
          tags: ['bancos', 'valor', 'p/vpa', 'subavaliado'],
          targetPrice: 32.00,
          stopLoss: 26.00,
        },
        {
          id: '4',
          title: 'Análise de Sentimento MGLU3 - Pressão Vendedora Continua',
          symbol: 'MGLU3',
          type: 'sentiment',
          recommendation: 'SELL',
          confidence: 68,
          createdAt: '2024-01-15T07:20:00Z',
          timestamp: '2024-01-15T07:20:00Z',
          analysis: 'Sentimento negativo persiste. Notícias sobre reestruturação geram incerteza.',
          agent: 'Agente de Sentimento',
          summary: 'Sentimento negativo persiste. Notícias sobre reestruturação geram incerteza.',
          content: 'A análise de sentimento do mercado em relação ao MGLU3 continua negativa...',
          author: 'Agente de Sentimento',
          views: 456,
          rating: 3.8,
          tags: ['varejo', 'reestruturação', 'sentimento', 'negativo'],
          targetPrice: 6.50,
          stopLoss: 9.20,
        },
      ];
      setAnalyses(mockAnalyses);
    } finally {
      setIsLoading(false);
    }
  };

  const filterAnalyses = () => {
    let filtered = [...analyses];

    // Filtro por termo de busca
    if (searchTerm) {
      filtered = filtered.filter(
        (analysis) =>
          analysis.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.symbol.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.tags?.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    // Filtro por tipo
    if (selectedType !== 'all') {
      filtered = filtered.filter((analysis) => analysis.type === selectedType);
    }

    // Filtro por recomendação
    if (selectedRecommendation !== 'all') {
      filtered = filtered.filter((analysis) => analysis.recommendation === selectedRecommendation);
    }

    // Ordenação
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'date':
          return new Date(b.createdAt || b.timestamp || 0).getTime() - new Date(a.createdAt || a.timestamp || 0).getTime();
        case 'confidence':
          return b.confidence - a.confidence;
        case 'rating':
          return (b.rating || 0) - (a.rating || 0);
        default:
          return 0;
      }
    });

    setFilteredAnalyses(filtered);
  };

  const getTypeIcon = (type: Analysis['type']) => {
    switch (type) {
      case 'technical':
        return <TrendingUp className="w-4 h-4" />;
      case 'fundamental':
        return <FileText className="w-4 h-4" />;
      case 'quantitative':
        return <TrendingDown className="w-4 h-4" />;
      case 'sentiment':
        return '💭';
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
      case 'sentiment':
        return 'Sentimento';
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
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-secondary-900 mb-2">Análises</h1>
          <p className="text-secondary-600">
            Análises detalhadas geradas pelos agentes de IA
          </p>
        </div>

        {/* Filtros e Busca */}
        <div className="card mb-6">
          <div className="card-content">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
              {/* Busca */}
              <div className="lg:col-span-2">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-secondary-400 w-4 h-4" />
                  <input
                    type="text"
                    placeholder="Buscar análises..."
                    className="input pl-10"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
              </div>

              {/* Filtro por Tipo */}
              <div>
                <select
                  className="input"
                  value={selectedType}
                  onChange={(e) => setSelectedType(e.target.value)}
                >
                  <option value="all">Todos os Tipos</option>
                  <option value="technical">Técnica</option>
                  <option value="fundamental">Fundamentalista</option>
                  <option value="quantitative">Quantitativa</option>
                  <option value="sentiment">Sentimento</option>
                </select>
              </div>

              {/* Filtro por Recomendação */}
              <div>
                <select
                  className="input"
                  value={selectedRecommendation}
                  onChange={(e) => setSelectedRecommendation(e.target.value)}
                >
                  <option value="all">Todas Recomendações</option>
                  <option value="buy">Compra</option>
                  <option value="hold">Manter</option>
                  <option value="sell">Venda</option>
                </select>
              </div>

              {/* Ordenação */}
              <div>
                <select
                  className="input"
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value as 'date' | 'confidence' | 'rating')}
                >
                  <option value="date">Mais Recentes</option>
                  <option value="confidence">Maior Confiança</option>
                  <option value="rating">Melhor Avaliação</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div className="card mb-6">
            <div className="card-content">
              <div className="text-center py-4">
                <p className="text-danger-600 text-sm mb-2">{error}</p>
                <button
                  onClick={loadAnalyses}
                  className="btn-primary btn-sm"
                >
                  Tentar Novamente
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Lista de Análises */}
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
          {filteredAnalyses.map((analysis) => (
            <div
              key={analysis.id}
              className="card hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => setSelectedAnalysis(analysis)}
            >
              <div className="card-content">
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
                  
                  <span className={`
                    px-2 py-1 text-xs font-medium rounded-full
                    ${getRecommendationColor(analysis.recommendation)}
                  `}>
                    {getRecommendationLabel(analysis.recommendation)}
                  </span>
                </div>
                
                <h3 className="font-semibold text-secondary-900 mb-2 line-clamp-2">
                  {analysis.title}
                </h3>
                
                <p className="text-sm text-secondary-600 mb-4 line-clamp-3">
                  {analysis.summary}
                </p>
                
                <div className="flex items-center justify-between text-xs text-secondary-500 mb-3">
                  <div className="flex items-center space-x-3">
                    <div className="flex items-center space-x-1">
                      <Calendar className="w-3 h-3" />
                      <span>{formatDate(analysis.createdAt || analysis.timestamp || '')}</span>
                    </div>
                    <div className="flex items-center space-x-1">
                      <Eye className="w-3 h-3" />
                      <span>{analysis.views}</span>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-1">
                    <Star className="w-3 h-3 text-warning-500" />
                    <span>{analysis.rating?.toFixed(1) || 'N/A'}</span>
                  </div>
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="text-xs text-secondary-600">
                    por {analysis.author}
                  </div>
                  
                  <div className="flex items-center space-x-1 text-xs">
                    <span className="text-secondary-600">Confiança:</span>
                    <span className="font-medium text-primary-600">
                      {analysis.confidence}%
                    </span>
                  </div>
                </div>
                
                {/* Tags */}
                <div className="flex flex-wrap gap-1 mt-3">
                  {analysis.tags?.slice(0, 3).map((tag) => (
                    <span
                      key={tag}
                      className="px-2 py-1 text-xs bg-secondary-100 text-secondary-700 rounded"
                    >
                      {tag}
                    </span>
                  ))}
                  {(analysis.tags?.length || 0) > 3 && (
                    <span className="px-2 py-1 text-xs bg-secondary-100 text-secondary-700 rounded">
                      +{(analysis.tags?.length || 0) - 3}
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {filteredAnalyses.length === 0 && !isLoading && (
          <div className="card">
            <div className="card-content">
              <div className="text-center py-12">
                <FileText className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-secondary-900 mb-2">
                  Nenhuma análise encontrada
                </h3>
                <p className="text-secondary-600">
                  Tente ajustar os filtros ou termos de busca
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Modal de Análise Detalhada */}
      {selectedAnalysis && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h2 className="text-2xl font-bold text-secondary-900 mb-2">
                    {selectedAnalysis.title}
                  </h2>
                  <div className="flex items-center space-x-4 text-sm text-secondary-600">
                    <span>{selectedAnalysis.symbol}</span>
                    <span>•</span>
                    <span>{getTypeLabel(selectedAnalysis.type)}</span>
                    <span>•</span>
                    <span>{formatDate(selectedAnalysis.createdAt || selectedAnalysis.timestamp)}</span>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedAnalysis(null)}
                  className="text-secondary-400 hover:text-secondary-600"
                >
                  ✕
                </button>
              </div>
              
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2">
                  <div className="prose max-w-none">
                    <p className="text-secondary-700 leading-relaxed">
                      {selectedAnalysis.content}
                    </p>
                  </div>
                  
                  {selectedAnalysis.symbol !== 'SETOR' && (
                    <div className="mt-6">
                      <h3 className="text-lg font-semibold mb-4">Gráfico</h3>
                      <StockChart symbol={selectedAnalysis.symbol} />
                    </div>
                  )}
                </div>
                
                <div className="space-y-4">
                  <div className="card">
                    <div className="card-content">
                      <h3 className="font-semibold mb-3">Recomendação</h3>
                      <div className={`
                        px-3 py-2 rounded-lg text-center font-medium
                        ${getRecommendationColor(selectedAnalysis.recommendation)}
                      `}>
                        {getRecommendationLabel(selectedAnalysis.recommendation)}
                      </div>
                      <div className="mt-3 text-center">
                        <div className="text-sm text-secondary-600">Confiança</div>
                        <div className="text-2xl font-bold text-primary-600">
                          {selectedAnalysis.confidence}%
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  {(selectedAnalysis.targetPrice || selectedAnalysis.stopLoss) && (
                    <div className="card">
                      <div className="card-content">
                        <h3 className="font-semibold mb-3">Alvos</h3>
                        {selectedAnalysis.targetPrice && (
                          <div className="flex justify-between mb-2">
                            <span className="text-secondary-600">Alvo:</span>
                            <span className="font-medium text-success-600">
                              R$ {selectedAnalysis.targetPrice.toFixed(2)}
                            </span>
                          </div>
                        )}
                        {selectedAnalysis.stopLoss && (
                          <div className="flex justify-between">
                            <span className="text-secondary-600">Stop Loss:</span>
                            <span className="font-medium text-danger-600">
                              R$ {selectedAnalysis.stopLoss.toFixed(2)}
                            </span>
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                  
                  <div className="card">
                    <div className="card-content">
                      <h3 className="font-semibold mb-3">Estatísticas</h3>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Visualizações:</span>
                          <span className="font-medium">{selectedAnalysis.views}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Avaliação:</span>
                          <span className="font-medium">{selectedAnalysis.rating?.toFixed(1) || 'N/A'}/5</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Autor:</span>
                          <span className="font-medium">{selectedAnalysis.author || selectedAnalysis.agent || 'N/A'}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="card">
                    <div className="card-content">
                      <h3 className="font-semibold mb-3">Tags</h3>
                      <div className="flex flex-wrap gap-2">
                        {selectedAnalysis.tags?.map((tag) => (
                          <span
                            key={tag}
                            className="px-2 py-1 text-xs bg-primary-100 text-primary-700 rounded"
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                  
                  <button className="btn-primary w-full">
                    <Download className="w-4 h-4 mr-2" />
                    Baixar Relatório
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Analytics;