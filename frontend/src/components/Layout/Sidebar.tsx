import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  BarChart3,
  Bot,
  Briefcase,
  Home,
  Settings,
  TrendingUp,
  X,
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';

interface SidebarProps {
  onClose?: () => void;
}

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: Home },
  { name: 'Análises', href: '/analytics', icon: TrendingUp },
  { name: 'Agentes', href: '/agents', icon: Bot },
  { name: 'Portfólio', href: '/portfolio', icon: Briefcase },
  { name: 'Configurações', href: '/settings', icon: Settings },
];

const Sidebar: React.FC<SidebarProps> = ({ onClose }) => {
  const location = useLocation();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    if (onClose) onClose();
  };

  return (
    <div className="flex grow flex-col gap-y-5 overflow-y-auto bg-white px-6 pb-4 shadow-soft">
      <div className="flex h-16 shrink-0 items-center justify-between">
        <div className="flex items-center">
          <BarChart3 className="h-8 w-8 text-primary-600" />
          <span className="ml-2 text-xl font-bold text-secondary-900">
            AccountaBot
          </span>
        </div>
        {onClose && (
          <button
            type="button"
            className="lg:hidden"
            onClick={onClose}
          >
            <X className="h-6 w-6 text-secondary-400" />
          </button>
        )}
      </div>
      
      <nav className="flex flex-1 flex-col">
        <ul role="list" className="flex flex-1 flex-col gap-y-7">
          <li>
            <ul role="list" className="-mx-2 space-y-1">
              {navigation.map((item) => {
                const isActive = location.pathname === item.href;
                return (
                  <li key={item.name}>
                    <Link
                      to={item.href}
                      className={`
                        group flex gap-x-3 rounded-md p-2 text-sm leading-6 font-semibold transition-colors
                        ${
                          isActive
                            ? 'bg-primary-50 text-primary-700'
                            : 'text-secondary-700 hover:text-primary-700 hover:bg-secondary-50'
                        }
                      `}
                      onClick={onClose}
                    >
                      <item.icon
                        className={`
                          h-6 w-6 shrink-0
                          ${
                            isActive
                              ? 'text-primary-700'
                              : 'text-secondary-400 group-hover:text-primary-700'
                          }
                        `}
                      />
                      {item.name}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </li>
          
          {/* Informações do usuário */}
          <li className="mt-auto">
            <div className="border-t border-secondary-200 pt-4">
              <div className="flex items-center gap-x-4 px-2 py-3 text-sm font-semibold leading-6 text-secondary-900">
                <div className="h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center">
                  <span className="text-sm font-medium text-white">
                    {user?.name?.charAt(0).toUpperCase() || 'U'}
                  </span>
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-secondary-900">
                    {user?.name || 'Usuário'}
                  </p>
                  <p className="text-xs text-secondary-500">
                    {user?.email || 'email@exemplo.com'}
                  </p>
                </div>
              </div>
              
              <button
                onClick={handleLogout}
                className="w-full text-left px-2 py-2 text-sm text-secondary-700 hover:bg-secondary-50 hover:text-secondary-900 rounded-md transition-colors"
              >
                Sair
              </button>
            </div>
          </li>
        </ul>
      </nav>
    </div>
  );
};

export default Sidebar;