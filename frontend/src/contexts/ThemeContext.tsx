import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

type Theme = 'light' | 'dark' | 'system';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  actualTheme: 'light' | 'dark';
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

interface ThemeProviderProps {
  children: ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [theme, setTheme] = useState<Theme>(() => {
    const savedTheme = localStorage.getItem('theme') as Theme;
    return savedTheme || 'system';
  });

  const [actualTheme, setActualTheme] = useState<'light' | 'dark'>('light');

  useEffect(() => {
    const root = window.document.documentElement;
    
    const updateTheme = () => {
      let newActualTheme: 'light' | 'dark';
      
      if (theme === 'system') {
        newActualTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
      } else {
        newActualTheme = theme;
      }
      
      setActualTheme(newActualTheme);
      
      root.classList.remove('light', 'dark');
      root.classList.add(newActualTheme);
      
      // Update CSS variables for theme
      if (newActualTheme === 'dark') {
        root.style.setProperty('--background', '0 0% 3.9%');
        root.style.setProperty('--foreground', '0 0% 98%');
        root.style.setProperty('--card', '0 0% 3.9%');
        root.style.setProperty('--card-foreground', '0 0% 98%');
        root.style.setProperty('--popover', '0 0% 3.9%');
        root.style.setProperty('--popover-foreground', '0 0% 98%');
        root.style.setProperty('--primary', '0 0% 98%');
        root.style.setProperty('--primary-foreground', '0 0% 9%');
        root.style.setProperty('--secondary', '0 0% 14.9%');
        root.style.setProperty('--secondary-foreground', '0 0% 98%');
        root.style.setProperty('--muted', '0 0% 14.9%');
        root.style.setProperty('--muted-foreground', '0 0% 63.9%');
        root.style.setProperty('--accent', '0 0% 14.9%');
        root.style.setProperty('--accent-foreground', '0 0% 98%');
        root.style.setProperty('--destructive', '0 62.8% 30.6%');
        root.style.setProperty('--destructive-foreground', '0 0% 98%');
        root.style.setProperty('--border', '0 0% 14.9%');
        root.style.setProperty('--input', '0 0% 14.9%');
        root.style.setProperty('--ring', '0 0% 83.1%');
      } else {
        root.style.setProperty('--background', '0 0% 100%');
        root.style.setProperty('--foreground', '0 0% 3.9%');
        root.style.setProperty('--card', '0 0% 100%');
        root.style.setProperty('--card-foreground', '0 0% 3.9%');
        root.style.setProperty('--popover', '0 0% 100%');
        root.style.setProperty('--popover-foreground', '0 0% 3.9%');
        root.style.setProperty('--primary', '0 0% 9%');
        root.style.setProperty('--primary-foreground', '0 0% 98%');
        root.style.setProperty('--secondary', '0 0% 96.1%');
        root.style.setProperty('--secondary-foreground', '0 0% 9%');
        root.style.setProperty('--muted', '0 0% 96.1%');
        root.style.setProperty('--muted-foreground', '0 0% 45.1%');
        root.style.setProperty('--accent', '0 0% 96.1%');
        root.style.setProperty('--accent-foreground', '0 0% 9%');
        root.style.setProperty('--destructive', '0 84.2% 60.2%');
        root.style.setProperty('--destructive-foreground', '0 0% 98%');
        root.style.setProperty('--border', '0 0% 89.8%');
        root.style.setProperty('--input', '0 0% 89.8%');
        root.style.setProperty('--ring', '0 0% 3.9%');
      }
    };

    updateTheme();
    localStorage.setItem('theme', theme);

    if (theme === 'system') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      mediaQuery.addEventListener('change', updateTheme);
      return () => mediaQuery.removeEventListener('change', updateTheme);
    }
  }, [theme]);

  const value: ThemeContextType = {
    theme,
    setTheme,
    actualTheme,
  };

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
};