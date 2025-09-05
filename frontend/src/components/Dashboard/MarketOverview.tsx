import React, { useState, useEffect } from 'react';
import { TrendingUp, TrendingDown, ArrowUpRight, ArrowDownRight } from 'lucide-react';
import { apiService } from '../../services/apiService';
import LoadingSpinner from '../UI/LoadingSpinner';

interface MarketIndex {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
}

const MarketOverview: React.FC = () => {
  const [marketData, setMarketData] = useState<MarketIndex[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadMarketData();
  }, []);

  const loadMarketData = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getMarketOverview();
      setMarketData(data);
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar dados do mercado:', err);
      setError('Erro ao carregar dados');
      // Dados mock para desenvolvimento
      setMarketData([
        {
          symbol: 'IBOV',
          name: 'Ibovespa',
          price: 126543.21,
          change: 1234.56,
          changePercent: 0.98,
        },
        {
          symbol: 'IFIX',
          name: 'IFIX',
          price: 3245.67,
          change: -23.45,
          changePercent: -0.72,
        },
        {
          symbol: 'SMLL',
          name: 'Small Cap',
          price: 4567.89,
          change: 45.67,
          changePercent: 1.01,
        },
        {
          symbol: 'IDIV',
          name: 'Dividendos',
          price: 8901.23,
          change: 12.34,
          changePercent: 0.14,
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Visão Geral do Mercado</h3>
        </div>
        <div className="card-content">
          <div className="flex items-center justify-center h-48">
            <LoadingSpinner size="lg" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">Visão Geral do Mercado</h3>
        <p className="card-description">
          Principais índices do mercado brasileiro
        </p>
      </div>
      <div className="card-content">
        {error && (
          <div className="text-center py-4">
            <p className="text-danger-600 text-sm mb-2">{error}</p>
            <button
              onClick={loadMarketData}
              className="btn-primary btn-sm"
            >
              Tentar Novamente
            </button>
          </div>
        )}
        
        <div className="space-y-4">
          {marketData.map((index) => {
            const isPositive = index.change >= 0;
            
            return (
              <div
                key={index.symbol}
                className="flex items-center justify-between p-3 rounded-lg bg-secondary-50 hover:bg-secondary-100 transition-colors"
              >
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <span className="font-medium text-secondary-900">
                      {index.symbol}
                    </span>
                    <span className="text-sm text-secondary-600">
                      {index.name}
                    </span>
                  </div>
                  <div className="text-lg font-semibold text-secondary-900">
                    {index.price.toLocaleString('pt-BR', {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}
                  </div>
                </div>
                
                <div className="text-right">
                  <div className={`
                    flex items-center space-x-1 text-sm font-medium
                    ${isPositive ? 'text-success-600' : 'text-danger-600'}
                  `}>
                    {isPositive ? (
                      <ArrowUpRight className="w-4 h-4" />
                    ) : (
                      <ArrowDownRight className="w-4 h-4" />
                    )}
                    <span>
                      {Math.abs(index.changePercent).toFixed(2)}%
                    </span>
                  </div>
                  <div className={`
                    text-sm
                    ${isPositive ? 'text-success-600' : 'text-danger-600'}
                  `}>
                    {isPositive ? '+' : ''}
                    {index.change.toLocaleString('pt-BR', {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
        
        <div className="mt-4 pt-4 border-t border-secondary-200">
          <div className="flex items-center justify-between text-sm text-secondary-600">
            <span>Última atualização:</span>
            <span>{new Date().toLocaleTimeString('pt-BR')}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MarketOverview;