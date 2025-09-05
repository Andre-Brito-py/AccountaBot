import React, { useState, useEffect } from 'react';
import { PieChart, TrendingUp, TrendingDown, Plus, Edit, Trash2, DollarSign, Percent, Calendar, BarChart3, Target, AlertTriangle } from 'lucide-react';
import { apiService, Portfolio, Position } from '../services/apiService';
import LoadingSpinner from '../components/UI/LoadingSpinner';
import StockChart from '../components/Charts/StockChart';

// Interface Position agora importada do apiService

// Interface Portfolio agora importada do apiService

interface PortfolioSummary {
  totalValue: number;
  totalCost: number;
  totalGainLoss: number;
  totalGainLossPercent: number;
  dayChange: number;
  dayChangePercent: number;
  bestPerformer: Position | null;
  worstPerformer: Position | null;
  sectorAllocation: { sector: string; value: number; percentage: number }[];
}

const PortfolioPage: React.FC = () => {
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio | null>(null);
  const [portfolioSummary, setPortfolioSummary] = useState<PortfolioSummary | null>(null);
  const [selectedPosition, setSelectedPosition] = useState<Position | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAddPosition, setShowAddPosition] = useState(false);
  const [activeTab, setActiveTab] = useState<'positions' | 'allocation' | 'performance'>('positions');

  useEffect(() => {
    loadPortfolios();
  }, []);

  useEffect(() => {
    if (selectedPortfolio) {
      calculatePortfolioSummary(selectedPortfolio);
    }
  }, [selectedPortfolio]);

  const loadPortfolios = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getPortfolios();
      setPortfolios(data);
      if (data.length > 0) {
        setSelectedPortfolio(data[0]);
      }
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar portfólios:', err);
      setError('Erro ao carregar dados');
      // Dados mock para desenvolvimento
      const mockPortfolios: Portfolio[] = [
          {
            id: '1',
            name: 'Portfólio Principal',
            totalValue: 125000.00,
            totalCost: 120000.00,
            totalGainLoss: 5000.00,
            totalGainLossPercent: 4.17,
            createdAt: '2024-01-01T00:00:00Z',
            lastUpdate: '2024-01-15T10:30:00Z',
          positions: [
            {
              id: '1',
              symbol: 'PETR4',
              name: 'Petrobras PN',
              quantity: 1000,
              avgPrice: 28.50,
              currentPrice: 32.45,
              totalValue: 32450.00,
              totalCost: 28500.00,
              gainLoss: 3950.00,
              gainLossPercent: 13.86,
              sector: 'Energia',
              weight: 25.96,
              lastUpdate: '2024-01-15T10:30:00Z',
            },
            {
              id: '2',
              symbol: 'VALE3',
              name: 'Vale ON',
              quantity: 500,
              avgPrice: 65.20,
              currentPrice: 68.90,
              totalValue: 34450.00,
              totalCost: 32600.00,
              gainLoss: 1850.00,
              gainLossPercent: 5.67,
              sector: 'Mineração',
              weight: 27.56,
              lastUpdate: '2024-01-15T10:30:00Z',
            },
            {
              id: '3',
              symbol: 'ITUB4',
              name: 'Itaú Unibanco PN',
              quantity: 800,
              avgPrice: 26.75,
              currentPrice: 28.75,
              totalValue: 23000.00,
              totalCost: 21400.00,
              gainLoss: 1600.00,
              gainLossPercent: 7.48,
              sector: 'Financeiro',
              weight: 18.40,
              lastUpdate: '2024-01-15T10:30:00Z',
            },
            {
              id: '4',
              symbol: 'BBDC4',
              name: 'Bradesco PN',
              quantity: 1200,
              avgPrice: 14.80,
              currentPrice: 15.60,
              totalValue: 18720.00,
              totalCost: 17760.00,
              gainLoss: 960.00,
              gainLossPercent: 5.41,
              sector: 'Financeiro',
              weight: 14.98,
              lastUpdate: '2024-01-15T10:30:00Z',
            },
            {
              id: '5',
              symbol: 'ABEV3',
              name: 'Ambev ON',
              quantity: 1300,
              avgPrice: 12.50,
              currentPrice: 12.34,
              totalValue: 16042.00,
              totalCost: 16250.00,
              gainLoss: -208.00,
              gainLossPercent: -1.28,
              sector: 'Consumo',
              weight: 12.83,
              lastUpdate: '2024-01-15T10:30:00Z',
            },
          ],
        },
      ];
      setPortfolios(mockPortfolios);
      setSelectedPortfolio(mockPortfolios[0]);
    } finally {
      setIsLoading(false);
    }
  };

  const calculatePortfolioSummary = (portfolio: Portfolio) => {
    const positions = portfolio.positions;
    
    // Melhor e pior performance
    const sortedByPerformance = [...positions].sort((a, b) => b.gainLossPercent - a.gainLossPercent);
    const bestPerformer = sortedByPerformance[0] || null;
    const worstPerformer = sortedByPerformance[sortedByPerformance.length - 1] || null;
    
    // Alocação por setor
    const sectorMap = new Map<string, number>();
    positions.forEach(position => {
      const current = sectorMap.get(position.sector) || 0;
      sectorMap.set(position.sector, current + position.totalValue);
    });
    
    const sectorAllocation = Array.from(sectorMap.entries()).map(([sector, value]) => ({
      sector,
      value,
      percentage: (value / portfolio.totalValue) * 100,
    })).sort((a, b) => b.value - a.value);
    
    const summary: PortfolioSummary = {
      totalValue: portfolio.totalValue,
      totalCost: portfolio.totalCost,
      totalGainLoss: portfolio.totalGainLoss,
      totalGainLossPercent: portfolio.totalGainLossPercent,
      dayChange: 2450.00, // Mock data
      dayChangePercent: 2.01, // Mock data
      bestPerformer,
      worstPerformer,
      sectorAllocation,
    };
    
    setPortfolioSummary(summary);
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const formatPercent = (value: number) => {
    return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
  };

  const getSectorColor = (index: number) => {
    const colors = [
      'bg-blue-500',
      'bg-green-500',
      'bg-yellow-500',
      'bg-red-500',
      'bg-purple-500',
      'bg-indigo-500',
      'bg-pink-500',
      'bg-gray-500',
    ];
    return colors[index % colors.length];
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
            <h1 className="text-3xl font-bold text-secondary-900 mb-2">Portfólio</h1>
            <p className="text-secondary-600">
              Gerencie seus investimentos e acompanhe performance
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <select
              className="input"
              value={selectedPortfolio?.id || ''}
              onChange={(e) => {
                const portfolio = portfolios.find(p => p.id === e.target.value);
                setSelectedPortfolio(portfolio || null);
              }}
            >
              {portfolios.map(portfolio => (
                <option key={portfolio.id} value={portfolio.id}>
                  {portfolio.name}
                </option>
              ))}
            </select>
            <button
              onClick={() => setShowAddPosition(true)}
              className="btn-primary"
            >
              <Plus className="w-4 h-4 mr-2" />
              Adicionar Posição
            </button>
          </div>
        </div>

        {error && (
          <div className="card mb-6">
            <div className="card-content">
              <div className="text-center py-4">
                <p className="text-danger-600 text-sm mb-2">{error}</p>
                <button
                  onClick={loadPortfolios}
                  className="btn-primary btn-sm"
                >
                  Tentar Novamente
                </button>
              </div>
            </div>
          </div>
        )}

        {selectedPortfolio && portfolioSummary && (
          <>
            {/* Resumo do Portfólio */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              <div className="card">
                <div className="card-content">
                  <div className="flex items-center justify-between mb-2">
                    <div className="text-sm text-secondary-600">Valor Total</div>
                    <DollarSign className="w-4 h-4 text-primary-600" />
                  </div>
                  <div className="text-2xl font-bold text-secondary-900">
                    {formatCurrency(portfolioSummary.totalValue)}
                  </div>
                  <div className={`text-sm ${
                    portfolioSummary.dayChange >= 0 ? 'text-success-600' : 'text-danger-600'
                  }`}>
                    {formatPercent(portfolioSummary.dayChangePercent)} hoje
                  </div>
                </div>
              </div>
              
              <div className="card">
                <div className="card-content">
                  <div className="flex items-center justify-between mb-2">
                    <div className="text-sm text-secondary-600">Ganho/Perda</div>
                    <Percent className="w-4 h-4 text-primary-600" />
                  </div>
                  <div className={`text-2xl font-bold ${
                    portfolioSummary.totalGainLoss >= 0 ? 'text-success-600' : 'text-danger-600'
                  }`}>
                    {formatCurrency(portfolioSummary.totalGainLoss)}
                  </div>
                  <div className={`text-sm ${
                    portfolioSummary.totalGainLoss >= 0 ? 'text-success-600' : 'text-danger-600'
                  }`}>
                    {formatPercent(portfolioSummary.totalGainLossPercent)}
                  </div>
                </div>
              </div>
              
              <div className="card">
                <div className="card-content">
                  <div className="flex items-center justify-between mb-2">
                    <div className="text-sm text-secondary-600">Melhor Ação</div>
                    <TrendingUp className="w-4 h-4 text-success-600" />
                  </div>
                  <div className="text-lg font-bold text-secondary-900">
                    {portfolioSummary.bestPerformer?.symbol || 'N/A'}
                  </div>
                  <div className="text-sm text-success-600">
                    {portfolioSummary.bestPerformer ? formatPercent(portfolioSummary.bestPerformer.gainLossPercent) : 'N/A'}
                  </div>
                </div>
              </div>
              
              <div className="card">
                <div className="card-content">
                  <div className="flex items-center justify-between mb-2">
                    <div className="text-sm text-secondary-600">Pior Ação</div>
                    <TrendingDown className="w-4 h-4 text-danger-600" />
                  </div>
                  <div className="text-lg font-bold text-secondary-900">
                    {portfolioSummary.worstPerformer?.symbol || 'N/A'}
                  </div>
                  <div className="text-sm text-danger-600">
                    {portfolioSummary.worstPerformer ? formatPercent(portfolioSummary.worstPerformer.gainLossPercent) : 'N/A'}
                  </div>
                </div>
              </div>
            </div>

            {/* Tabs */}
            <div className="card mb-6">
              <div className="card-content">
                <div className="flex space-x-1 bg-secondary-100 p-1 rounded-lg">
                  {[
                    { key: 'positions', label: 'Posições', icon: BarChart3 },
                    { key: 'allocation', label: 'Alocação', icon: PieChart },
                    { key: 'performance', label: 'Performance', icon: TrendingUp },
                  ].map(({ key, label, icon: Icon }) => (
                    <button
                      key={key}
                      className={`
                        flex-1 flex items-center justify-center px-3 py-2 text-sm font-medium rounded-md transition-colors
                        ${
                          activeTab === key
                            ? 'bg-white text-secondary-900 shadow-sm'
                            : 'text-secondary-600 hover:text-secondary-900'
                        }
                      `}
                      onClick={() => setActiveTab(key as any)}
                    >
                      <Icon className="w-4 h-4 mr-2" />
                      {label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Conteúdo das Tabs */}
            {activeTab === 'positions' && (
              <div className="card">
                <div className="card-header">
                  <h3 className="card-title">Posições</h3>
                  <p className="card-description">
                    {selectedPortfolio.positions.length} posições ativas
                  </p>
                </div>
                <div className="card-content">
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b border-secondary-200">
                          <th className="text-left py-3 px-4 font-medium text-secondary-700">Ativo</th>
                          <th className="text-right py-3 px-4 font-medium text-secondary-700">Quantidade</th>
                          <th className="text-right py-3 px-4 font-medium text-secondary-700">Preço Médio</th>
                          <th className="text-right py-3 px-4 font-medium text-secondary-700">Preço Atual</th>
                          <th className="text-right py-3 px-4 font-medium text-secondary-700">Valor Total</th>
                          <th className="text-right py-3 px-4 font-medium text-secondary-700">Ganho/Perda</th>
                          <th className="text-right py-3 px-4 font-medium text-secondary-700">%</th>
                          <th className="text-right py-3 px-4 font-medium text-secondary-700">Peso</th>
                          <th className="text-center py-3 px-4 font-medium text-secondary-700">Ações</th>
                        </tr>
                      </thead>
                      <tbody>
                        {selectedPortfolio.positions.map((position) => (
                          <tr
                            key={position.id}
                            className="border-b border-secondary-100 hover:bg-secondary-50 cursor-pointer"
                            onClick={() => setSelectedPosition(position)}
                          >
                            <td className="py-3 px-4">
                              <div>
                                <div className="font-medium text-secondary-900">
                                  {position.symbol}
                                </div>
                                <div className="text-sm text-secondary-600">
                                  {position.name}
                                </div>
                              </div>
                            </td>
                            <td className="text-right py-3 px-4 text-secondary-900">
                              {position.quantity.toLocaleString()}
                            </td>
                            <td className="text-right py-3 px-4 text-secondary-900">
                              {formatCurrency(position.avgPrice)}
                            </td>
                            <td className="text-right py-3 px-4 text-secondary-900">
                              {formatCurrency(position.currentPrice)}
                            </td>
                            <td className="text-right py-3 px-4 font-medium text-secondary-900">
                              {formatCurrency(position.totalValue)}
                            </td>
                            <td className={`text-right py-3 px-4 font-medium ${
                              position.gainLoss >= 0 ? 'text-success-600' : 'text-danger-600'
                            }`}>
                              {formatCurrency(position.gainLoss)}
                            </td>
                            <td className={`text-right py-3 px-4 font-medium ${
                              position.gainLoss >= 0 ? 'text-success-600' : 'text-danger-600'
                            }`}>
                              {formatPercent(position.gainLossPercent)}
                            </td>
                            <td className="text-right py-3 px-4 text-secondary-900">
                              {position.weight.toFixed(1)}%
                            </td>
                            <td className="text-center py-3 px-4">
                              <div className="flex items-center justify-center space-x-2">
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    // Implementar edição
                                  }}
                                  className="p-1 hover:bg-secondary-200 rounded transition-colors"
                                  title="Editar"
                                >
                                  <Edit className="w-4 h-4 text-secondary-600" />
                                </button>
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    // Implementar exclusão
                                  }}
                                  className="p-1 hover:bg-danger-100 rounded transition-colors"
                                  title="Excluir"
                                >
                                  <Trash2 className="w-4 h-4 text-danger-600" />
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'allocation' && (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="card">
                  <div className="card-header">
                    <h3 className="card-title">Alocação por Setor</h3>
                  </div>
                  <div className="card-content">
                    <div className="space-y-4">
                      {portfolioSummary.sectorAllocation.map((sector, index) => (
                        <div key={sector.sector} className="flex items-center justify-between">
                          <div className="flex items-center space-x-3">
                            <div className={`w-4 h-4 rounded ${getSectorColor(index)}`}></div>
                            <span className="font-medium text-secondary-900">
                              {sector.sector}
                            </span>
                          </div>
                          <div className="text-right">
                            <div className="font-medium text-secondary-900">
                              {formatCurrency(sector.value)}
                            </div>
                            <div className="text-sm text-secondary-600">
                              {sector.percentage.toFixed(1)}%
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
                
                <div className="card">
                  <div className="card-header">
                    <h3 className="card-title">Distribuição Visual</h3>
                  </div>
                  <div className="card-content">
                    <div className="text-center py-12">
                      <PieChart className="w-16 h-16 text-secondary-400 mx-auto mb-4" />
                      <p className="text-secondary-600">
                        Gráfico de pizza em desenvolvimento
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'performance' && (
              <div className="card">
                <div className="card-header">
                  <h3 className="card-title">Performance do Portfólio</h3>
                </div>
                <div className="card-content">
                  <div className="text-center py-12">
                    <BarChart3 className="w-16 h-16 text-secondary-400 mx-auto mb-4" />
                    <p className="text-secondary-600">
                      Gráfico de performance em desenvolvimento
                    </p>
                  </div>
                </div>
              </div>
            )}
          </>
        )}

        {portfolios.length === 0 && !isLoading && (
          <div className="card">
            <div className="card-content">
              <div className="text-center py-12">
                <PieChart className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-secondary-900 mb-2">
                  Nenhum portfólio encontrado
                </h3>
                <p className="text-secondary-600 mb-4">
                  Crie seu primeiro portfólio para começar a acompanhar seus investimentos
                </p>
                <button className="btn-primary">
                  <Plus className="w-4 h-4 mr-2" />
                  Criar Portfólio
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Modal de Detalhes da Posição */}
      {selectedPosition && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex items-start justify-between mb-6">
                <div>
                  <h2 className="text-2xl font-bold text-secondary-900 mb-2">
                    {selectedPosition.symbol} - {selectedPosition.name}
                  </h2>
                  <div className="flex items-center space-x-4 text-sm text-secondary-600">
                    <span>Setor: {selectedPosition.sector}</span>
                    <span>•</span>
                    <span>Peso: {selectedPosition.weight.toFixed(1)}%</span>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedPosition(null)}
                  className="text-secondary-400 hover:text-secondary-600"
                >
                  ✕
                </button>
              </div>
              
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2">
                  <StockChart symbol={selectedPosition.symbol} />
                </div>
                
                <div className="space-y-4">
                  <div className="card">
                    <div className="card-content">
                      <h3 className="font-semibold mb-3">Resumo da Posição</h3>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Quantidade:</span>
                          <span className="font-medium">{selectedPosition.quantity.toLocaleString()}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Preço Médio:</span>
                          <span className="font-medium">{formatCurrency(selectedPosition.avgPrice)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Preço Atual:</span>
                          <span className="font-medium">{formatCurrency(selectedPosition.currentPrice)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Valor Total:</span>
                          <span className="font-medium">{formatCurrency(selectedPosition.totalValue)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-secondary-600">Custo Total:</span>
                          <span className="font-medium">{formatCurrency(selectedPosition.totalCost)}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="card">
                    <div className="card-content">
                      <h3 className="font-semibold mb-3">Performance</h3>
                      <div className="text-center">
                        <div className={`text-2xl font-bold mb-2 ${
                          selectedPosition.gainLoss >= 0 ? 'text-success-600' : 'text-danger-600'
                        }`}>
                          {formatCurrency(selectedPosition.gainLoss)}
                        </div>
                        <div className={`text-lg ${
                          selectedPosition.gainLoss >= 0 ? 'text-success-600' : 'text-danger-600'
                        }`}>
                          {formatPercent(selectedPosition.gainLossPercent)}
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex space-x-2">
                    <button className="btn-outline flex-1">
                      <Edit className="w-4 h-4 mr-2" />
                      Editar
                    </button>
                    <button className="btn-danger flex-1">
                      <Trash2 className="w-4 h-4 mr-2" />
                      Excluir
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Adicionar Posição */}
      {showAddPosition && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-bold text-secondary-900">
                  Adicionar Posição
                </h2>
                <button
                  onClick={() => setShowAddPosition(false)}
                  className="text-secondary-400 hover:text-secondary-600"
                >
                  ✕
                </button>
              </div>
              
              <div className="text-center py-12">
                <Plus className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-secondary-900 mb-2">
                  Adicionar Nova Posição
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

export default PortfolioPage;