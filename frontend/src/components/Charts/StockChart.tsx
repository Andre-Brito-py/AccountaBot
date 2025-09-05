import React, { useState, useEffect } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts';
import { apiService } from '../../services/apiService';
import LoadingSpinner from '../UI/LoadingSpinner';

interface StockChartProps {
  symbol: string;
  height?: number;
  period?: string;
  type?: 'line' | 'area';
}

interface ChartData {
  date: string;
  price: number;
  volume?: number;
  timestamp: number;
}

const StockChart: React.FC<StockChartProps> = ({
  symbol,
  height = 400,
  period = '1M',
  type = 'area',
}) => {
  const [data, setData] = useState<ChartData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadChartData();
  }, [symbol, period]);

  const loadChartData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      let chartData: ChartData[];
      
      if (symbol === 'PORTFOLIO') {
        // Dados mock para o portfólio
        chartData = generateMockPortfolioData();
      } else {
        const historicalData = await apiService.getHistoricalData(symbol, period);
        chartData = historicalData.map((item: any) => ({
          date: new Date(item.timestamp).toLocaleDateString('pt-BR'),
          price: item.close,
          volume: item.volume,
          timestamp: item.timestamp,
        }));
      }
      
      setData(chartData);
    } catch (err) {
      console.error('Erro ao carregar dados do gráfico:', err);
      setError('Erro ao carregar dados do gráfico');
      // Fallback para dados mock
      setData(generateMockData());
    } finally {
      setIsLoading(false);
    }
  };

  const generateMockPortfolioData = (): ChartData[] => {
    const data: ChartData[] = [];
    const baseValue = 120000;
    const now = new Date();
    
    for (let i = 29; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      const variation = (Math.random() - 0.5) * 0.02; // ±1% variation
      const price = baseValue * (1 + variation * i * 0.1);
      
      data.push({
        date: date.toLocaleDateString('pt-BR'),
        price: Math.round(price * 100) / 100,
        timestamp: date.getTime(),
      });
    }
    
    return data;
  };

  const generateMockData = (): ChartData[] => {
    const data: ChartData[] = [];
    const basePrice = 100;
    const now = new Date();
    
    for (let i = 29; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      const variation = (Math.random() - 0.5) * 0.1; // ±5% variation
      const price = basePrice * (1 + variation);
      
      data.push({
        date: date.toLocaleDateString('pt-BR'),
        price: Math.round(price * 100) / 100,
        volume: Math.floor(Math.random() * 1000000),
        timestamp: date.getTime(),
      });
    }
    
    return data;
  };

  const formatTooltipValue = (value: number, name: string) => {
    if (name === 'price') {
      return [`R$ ${value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`, 'Preço'];
    }
    if (name === 'volume') {
      return [value.toLocaleString('pt-BR'), 'Volume'];
    }
    return [value, name];
  };

  const formatYAxisTick = (value: number) => {
    if (symbol === 'PORTFOLIO') {
      return `R$ ${(value / 1000).toFixed(0)}k`;
    }
    return `R$ ${value.toFixed(2)}`;
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center" style={{ height }}>
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error && data.length === 0) {
    return (
      <div className="flex items-center justify-center" style={{ height }}>
        <div className="text-center">
          <p className="text-danger-600 mb-2">{error}</p>
          <button
            onClick={loadChartData}
            className="btn-primary btn-sm"
          >
            Tentar Novamente
          </button>
        </div>
      </div>
    );
  }

  const isPositiveTrend = data.length > 1 && data[data.length - 1].price > data[0].price;
  const chartColor = isPositiveTrend ? '#22c55e' : '#ef4444';
  const fillColor = isPositiveTrend ? 'rgba(34, 197, 94, 0.1)' : 'rgba(239, 68, 68, 0.1)';

  return (
    <div style={{ height }}>
      <ResponsiveContainer width="100%" height="100%">
        {type === 'area' ? (
          <AreaChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
            <defs>
              <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={chartColor} stopOpacity={0.3} />
                <stop offset="95%" stopColor={chartColor} stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
            <XAxis
              dataKey="date"
              stroke="#64748b"
              fontSize={12}
              tickLine={false}
              axisLine={false}
            />
            <YAxis
              stroke="#64748b"
              fontSize={12}
              tickLine={false}
              axisLine={false}
              tickFormatter={formatYAxisTick}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: 'white',
                border: '1px solid #e2e8f0',
                borderRadius: '8px',
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
              }}
              formatter={formatTooltipValue}
              labelStyle={{ color: '#374151' }}
            />
            <Area
              type="monotone"
              dataKey="price"
              stroke={chartColor}
              strokeWidth={2}
              fill="url(#colorPrice)"
              dot={false}
              activeDot={{ r: 4, stroke: chartColor, strokeWidth: 2, fill: 'white' }}
            />
          </AreaChart>
        ) : (
          <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
            <XAxis
              dataKey="date"
              stroke="#64748b"
              fontSize={12}
              tickLine={false}
              axisLine={false}
            />
            <YAxis
              stroke="#64748b"
              fontSize={12}
              tickLine={false}
              axisLine={false}
              tickFormatter={formatYAxisTick}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: 'white',
                border: '1px solid #e2e8f0',
                borderRadius: '8px',
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
              }}
              formatter={formatTooltipValue}
              labelStyle={{ color: '#374151' }}
            />
            <Line
              type="monotone"
              dataKey="price"
              stroke={chartColor}
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4, stroke: chartColor, strokeWidth: 2, fill: 'white' }}
            />
          </LineChart>
        )}
      </ResponsiveContainer>
    </div>
  );
};

export default StockChart;