import React, { useState, useEffect } from 'react';
import { TrendingUp, TrendingDown, ArrowUpRight, ArrowDownRight } from 'lucide-react';
import { apiService } from '../../services/apiService';
import LoadingSpinner from '../UI/LoadingSpinner';

interface TopMover {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
}

const TopMovers: React.FC = () => {
  const [topGainers, setTopGainers] = useState<TopMover[]>([]);
  const [topLosers, setTopLosers] = useState<TopMover[]>([]);
  const [activeTab, setActiveTab] = useState<'gainers' | 'losers'>('gainers');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadTopMovers();
  }, []);

  const loadTopMovers = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getTopMovers();
      setTopGainers(data.gainers || []);
      setTopLosers(data.losers || []);
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar top movers:', err);
      setError('Erro ao carregar dados');
      // Dados mock para desenvolvimento
      setTopGainers([
        {
          symbol: 'PETR4',
          name: 'Petrobras PN',
          price: 32.45,
          change: 2.15,
          changePercent: 7.08,
          volume: 45678900,
        },
        {
          symbol: 'VALE3',
          name: 'Vale ON',
          price: 68.90,
          change: 3.20,
          changePercent: 4.87,
          volume: 23456789,
        },
        {
          symbol: 'ITUB4',
          name: 'Itaú Unibanco PN',
          price: 28.75,
          change: 1.05,
          changePercent: 3.79,
          volume: 34567890,
        },
        {
          symbol: 'BBDC4',
          name: 'Bradesco PN',
          price: 15.60,
          change: 0.45,
          changePercent: 2.97,
          volume: 12345678,
        },
        {
          symbol: 'ABEV3',
          name: 'Ambev ON',
          price: 12.34,
          change: 0.30,
          changePercent: 2.49,
          volume: 56789012,
        },
      ]);
      
      setTopLosers([
        {
          symbol: 'MGLU3',
          name: 'Magazine Luiza ON',
          price: 8.45,
          change: -0.65,
          changePercent: -7.14,
          volume: 78901234,
        },
        {
          symbol: 'VVAR3',
          name: 'Via ON',
          price: 4.20,
          change: -0.28,
          changePercent: -6.25,
          volume: 23456789,
        },
        {
          symbol: 'AMER3',
          name: 'Americanas ON',
          price: 1.85,
          change: -0.12,
          changePercent: -6.09,
          volume: 45678901,
        },
        {
          symbol: 'CYRE3',
          name: 'Cyrela ON',
          price: 18.90,
          change: -0.95,
          changePercent: -4.78,
          volume: 12345678,
        },
        {
          symbol: 'HAPV3',
          name: 'Hapvida ON',
          price: 6.75,
          change: -0.25,
          changePercent: -3.57,
          volume: 34567890,
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const formatVolume = (volume: number) => {
    if (volume >= 1000000) {
      return `${(volume / 1000000).toFixed(1)}M`;
    }
    if (volume >= 1000) {
      return `${(volume / 1000).toFixed(1)}K`;
    }
    return volume.toString();
  };

  const currentData = activeTab === 'gainers' ? topGainers : topLosers;

  if (isLoading) {
    return (
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Maiores Variações</h3>
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
        <h3 className="card-title">Maiores Variações</h3>
        <p className="card-description">
          Ações com maior movimento no dia
        </p>
      </div>
      <div className="card-content">
        {error && (
          <div className="text-center py-4 mb-4">
            <p className="text-danger-600 text-sm mb-2">{error}</p>
            <button
              onClick={loadTopMovers}
              className="btn-primary btn-sm"
            >
              Tentar Novamente
            </button>
          </div>
        )}
        
        {/* Tabs */}
        <div className="flex space-x-1 mb-4 bg-secondary-100 p-1 rounded-lg">
          <button
            className={`
              flex-1 flex items-center justify-center px-3 py-2 text-sm font-medium rounded-md transition-colors
              ${
                activeTab === 'gainers'
                  ? 'bg-white text-success-700 shadow-sm'
                  : 'text-secondary-600 hover:text-secondary-900'
              }
            `}
            onClick={() => setActiveTab('gainers')}
          >
            <TrendingUp className="w-4 h-4 mr-2" />
            Maiores Altas
          </button>
          <button
            className={`
              flex-1 flex items-center justify-center px-3 py-2 text-sm font-medium rounded-md transition-colors
              ${
                activeTab === 'losers'
                  ? 'bg-white text-danger-700 shadow-sm'
                  : 'text-secondary-600 hover:text-secondary-900'
              }
            `}
            onClick={() => setActiveTab('losers')}
          >
            <TrendingDown className="w-4 h-4 mr-2" />
            Maiores Baixas
          </button>
        </div>
        
        {/* Lista de ações */}
        <div className="space-y-3">
          {currentData.map((stock, index) => {
            const isPositive = stock.change >= 0;
            
            return (
              <div
                key={stock.symbol}
                className="flex items-center justify-between p-3 rounded-lg bg-secondary-50 hover:bg-secondary-100 transition-colors cursor-pointer"
              >
                <div className="flex items-center space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                    <span className="text-xs font-medium text-primary-700">
                      {index + 1}
                    </span>
                  </div>
                  <div>
                    <div className="font-medium text-secondary-900">
                      {stock.symbol}
                    </div>
                    <div className="text-sm text-secondary-600 truncate max-w-32">
                      {stock.name}
                    </div>
                  </div>
                </div>
                
                <div className="text-right">
                  <div className="font-medium text-secondary-900">
                    R$ {stock.price.toFixed(2)}
                  </div>
                  <div className={`
                    flex items-center space-x-1 text-sm font-medium
                    ${isPositive ? 'text-success-600' : 'text-danger-600'}
                  `}>
                    {isPositive ? (
                      <ArrowUpRight className="w-3 h-3" />
                    ) : (
                      <ArrowDownRight className="w-3 h-3" />
                    )}
                    <span>{Math.abs(stock.changePercent).toFixed(2)}%</span>
                  </div>
                  <div className="text-xs text-secondary-500">
                    Vol: {formatVolume(stock.volume)}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
        
        <div className="mt-4 pt-4 border-t border-secondary-200">
          <div className="text-center">
            <button className="btn-outline btn-sm">
              Ver Mais
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TopMovers;