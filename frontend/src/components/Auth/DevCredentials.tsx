import React, { useState } from 'react';
import { Info, Eye, EyeOff } from 'lucide-react';

interface DevCredentialsProps {
  onCredentialSelect: (email: string, password: string) => void;
}

const DevCredentials: React.FC<DevCredentialsProps> = ({ onCredentialSelect }) => {
  const [isVisible, setIsVisible] = useState(false);

  const credentials = [
    {
      label: 'Administrador',
      email: 'admin@accountabot.com',
      password: 'admin123',
      role: 'Admin'
    },
    {
      label: 'Usuário Demo',
      email: 'user@accountabot.com',
      password: 'user123',
      role: 'User'
    },
    {
      label: 'Demo Simples',
      email: 'demo@demo.com',
      password: '123456',
      role: 'User'
    }
  ];

  if (!isVisible) {
    return (
      <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
        <button
          type="button"
          onClick={() => setIsVisible(true)}
          className="flex items-center text-sm text-blue-700 hover:text-blue-800 transition-colors"
        >
          <Info className="w-4 h-4 mr-2" />
          Mostrar credenciais de desenvolvimento
        </button>
      </div>
    );
  }

  return (
    <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-md">
      <div className="flex items-center justify-between mb-3">
        <h4 className="text-sm font-medium text-blue-800 flex items-center">
          <Info className="w-4 h-4 mr-2" />
          Credenciais de Desenvolvimento
        </h4>
        <button
          type="button"
          onClick={() => setIsVisible(false)}
          className="text-blue-600 hover:text-blue-800 transition-colors"
        >
          <EyeOff className="w-4 h-4" />
        </button>
      </div>
      
      <div className="space-y-2">
        {credentials.map((cred, index) => (
          <div
            key={index}
            className="flex items-center justify-between p-2 bg-white rounded border border-blue-100 hover:border-blue-300 transition-colors"
          >
            <div className="flex-1">
              <div className="text-xs font-medium text-gray-700">{cred.label}</div>
              <div className="text-xs text-gray-500">{cred.email}</div>
              <div className="text-xs text-gray-400">Senha: {cred.password}</div>
            </div>
            <button
              type="button"
              onClick={() => onCredentialSelect(cred.email, cred.password)}
              className="px-3 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
            >
              Usar
            </button>
          </div>
        ))}
      </div>
      
      <p className="mt-3 text-xs text-blue-600">
        💡 Estas credenciais são apenas para desenvolvimento e demonstração.
      </p>
    </div>
  );
};

export default DevCredentials;