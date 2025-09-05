import React, { useState, useEffect } from 'react';
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  BarChart3,
  Activity,
  Users,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-react';
import { apiService } from '../services/apiService';
import LoadingSpinner from '../components/UI/LoadingSpinner';
import StockChart from '../components/Charts/StockChart';
import MarketOverview from '../components/Dashboard/MarketOverview';
import TopMovers from '../components/Dashboard/TopMovers';
import RecentAnalyses from '../components/Dashboard/RecentAnalyses';
import AgentStatus from '../components/Dashboard/AgentStatus';

interface DashboardData {
  portfolioValue: number;
  portfolioChange: number;
  portfolioChangePercent: number;
  totalReturn: number;
  totalReturnPercent: number;
  activeAgents: number;
  totalAnalyses: number;
  marketStatus: 'OPEN' | 'CLOSED' | 'PRE_MARKET' | 'AFTER_HOURS';
}

const Dashboard: React.FC = () => {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setIsLoading(true);
      const data = await apiService.getDashboardData();
      setDashboardData(data);
      setError(null);
    } catch (err) {
      console.error('Erro ao carregar dados do dashboard:', err);
      setError('Erro ao carregar dados do dashboard');
      // Dados mock para desenvolvimento
      setDashboardData({
        portfolioValue: 125430.50,
        portfolioChange: 2340.25,
        portfolioChangePercent: 1.9,
        totalReturn: 15430.50,
        totalReturnPercent: 14.2,
        activeAgents: 5,
        totalAnalyses: 127,
        marketStatus: 'OPEN',
      });
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error && !dashboardData) {
    return (
      <div className="text-center py-12">
        <div className="text-danger-600 mb-4">{error}</div>
        <button
          onClick={loadDashboardData}
          className="btn-primary btn-md"
        >
          Tentar Novamente
        </button>
      </div>
    );
  }

  const stats = [
    {
      name: 'Valor do Portfólio',
      value: `R$ ${dashboardData?.portfolioValue.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
      change: dashboardData?.portfolioChange || 0,
      changePercent: dashboardData?.portfolioChangePercent || 0,
      icon: DollarSign,
    },
    {
      name: 'Retorno Total',
      value: `R$ ${dashboardData?.totalReturn.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
      change: dashboardData?.totalReturn || 0,
      changePercent: dashboardData?.totalReturnPercent || 0,
      icon: TrendingUp,
    },
    {
      name: 'Agentes Ativos',
      value: dashboardData?.activeAgents.toString() || '0',
      change: 2,
      changePercent: 40,
      icon: Users,
    },
    {
      name: 'Análises Realizadas',
      value: dashboardData?.totalAnalyses.toString() || '0',
      change: 12,
      changePercent: 10.4,
      icon: BarChart3,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-secondary-900">Dashboard</h1>
          <p className="text-secondary-600">
            Visão geral do seu portfólio e análises financeiras
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <div className={`
            inline-flex items-center px-3 py-1 rounded-full text-sm font-medium
            ${
              dashboardData?.marketStatus === 'OPEN'
                ? 'bg-success-100 text-success-800'
                : 'bg-secondary-100 text-secondary-800'
            }
          `}>
            <Activity className="w-4 h-4 mr-1" />
            {dashboardData?.marketStatus === 'OPEN' ? 'Mercado Aberto' : 'Mercado Fechado'}
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => {
          const Icon = stat.icon;
          const isPositive = stat.change >= 0;
          
          return (
            <div key={stat.name} className="card">
              <div className="card-content">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Icon className="h-8 w-8 text-secondary-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-secondary-500 truncate">
                        {stat.name}
                      </dt>
                      <dd className="flex items-baseline">
                        <div className="text-2xl font-semibold text-secondary-900">
                          {stat.value}
                        </div>
                        <div className={`
                          ml-2 flex items-baseline text-sm font-semibold
                          ${isPositive ? 'text-success-600' : 'text-danger-600'}
                        `}>
                          {isPositive ? (
                            <ArrowUpRight className="self-center flex-shrink-0 h-4 w-4" />
                          ) : (
                            <ArrowDownRight className="self-center flex-shrink-0 h-4 w-4" />
                          )}
                          <span className="sr-only">
                            {isPositive ? 'Aumentou' : 'Diminuiu'} em
                          </span>
                          {Math.abs(stat.changePercent)}%
                        </div>
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Charts and Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Portfolio Chart */}
        <div className="lg:col-span-2">
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Performance do Portfólio</h3>
              <p className="card-description">
                Evolução do valor do portfólio nos últimos 30 dias
              </p>
            </div>
            <div className="card-content">
              <StockChart symbol="PORTFOLIO" height={300} />
            </div>
          </div>
        </div>

        {/* Market Overview */}
        <div>
          <MarketOverview />
        </div>
      </div>

      {/* Bottom Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top Movers */}
        <TopMovers />
        
        {/* Recent Analyses */}
        <RecentAnalyses />
      </div>

      {/* Agent Status */}
      <AgentStatus />
    </div>
  );
};

export default Dashboard;