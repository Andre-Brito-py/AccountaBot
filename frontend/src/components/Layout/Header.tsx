import React, { useState } from 'react';
import { Bell, Menu, Moon, Search, Sun, Monitor } from 'lucide-react';
import { useTheme } from '../../contexts/ThemeContext';
import { useAuth } from '../../contexts/AuthContext';

interface HeaderProps {
  onMenuClick: () => void;
}

const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
  const { theme, setTheme } = useTheme();
  const { user } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [showThemeMenu, setShowThemeMenu] = useState(false);

  const themeOptions = [
    { value: 'light', label: 'Claro', icon: Sun },
    { value: 'dark', label: 'Escuro', icon: Moon },
    { value: 'system', label: 'Sistema', icon: Monitor },
  ];

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Implementar lógica de busca
    console.log('Buscar:', searchQuery);
  };

  return (
    <div className="sticky top-0 z-40 flex h-16 shrink-0 items-center gap-x-4 border-b border-secondary-200 bg-white px-4 shadow-sm sm:gap-x-6 sm:px-6 lg:px-8">
      {/* Botão do menu mobile */}
      <button
        type="button"
        className="-m-2.5 p-2.5 text-secondary-700 lg:hidden"
        onClick={onMenuClick}
      >
        <Menu className="h-6 w-6" />
      </button>

      {/* Separador */}
      <div className="h-6 w-px bg-secondary-200 lg:hidden" />

      <div className="flex flex-1 gap-x-4 self-stretch lg:gap-x-6">
        {/* Barra de busca */}
        <form className="relative flex flex-1" onSubmit={handleSearch}>
          <label htmlFor="search-field" className="sr-only">
            Buscar
          </label>
          <Search className="pointer-events-none absolute inset-y-0 left-0 h-full w-5 text-secondary-400 pl-3" />
          <input
            id="search-field"
            className="block h-full w-full border-0 py-0 pl-10 pr-0 text-secondary-900 placeholder:text-secondary-400 focus:ring-0 sm:text-sm bg-transparent"
            placeholder="Buscar ações, análises, agentes..."
            type="search"
            name="search"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </form>
        
        <div className="flex items-center gap-x-4 lg:gap-x-6">
          {/* Botão de notificações */}
          <button
            type="button"
            className="-m-2.5 p-2.5 text-secondary-400 hover:text-secondary-500 relative"
          >
            <Bell className="h-6 w-6" />
            {/* Indicador de notificação */}
            <span className="absolute -top-1 -right-1 h-2 w-2 rounded-full bg-danger-500"></span>
          </button>

          {/* Separador */}
          <div className="hidden lg:block lg:h-6 lg:w-px lg:bg-secondary-200" />

          {/* Seletor de tema */}
          <div className="relative">
            <button
              type="button"
              className="-m-2.5 p-2.5 text-secondary-400 hover:text-secondary-500"
              onClick={() => setShowThemeMenu(!showThemeMenu)}
            >
              {theme === 'light' && <Sun className="h-6 w-6" />}
              {theme === 'dark' && <Moon className="h-6 w-6" />}
              {theme === 'system' && <Monitor className="h-6 w-6" />}
            </button>

            {showThemeMenu && (
              <div className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                {themeOptions.map((option) => {
                  const Icon = option.icon;
                  return (
                    <button
                      key={option.value}
                      className={`
                        flex w-full items-center px-4 py-2 text-sm text-left hover:bg-secondary-50
                        ${theme === option.value ? 'bg-primary-50 text-primary-700' : 'text-secondary-700'}
                      `}
                      onClick={() => {
                        setTheme(option.value as any);
                        setShowThemeMenu(false);
                      }}
                    >
                      <Icon className="mr-3 h-4 w-4" />
                      {option.label}
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Avatar do usuário */}
          <div className="flex items-center gap-x-2">
            <div className="h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center">
              <span className="text-sm font-medium text-white">
                {user?.name?.charAt(0).toUpperCase() || 'U'}
              </span>
            </div>
            <span className="hidden lg:flex lg:items-center">
              <span className="ml-2 text-sm font-semibold leading-6 text-secondary-900">
                {user?.name || 'Usuário'}
              </span>
            </span>
          </div>
        </div>
      </div>

      {/* Overlay para fechar menu de tema */}
      {showThemeMenu && (
        <div
          className="fixed inset-0 z-0"
          onClick={() => setShowThemeMenu(false)}
        />
      )}
    </div>
  );
};

export default Header;