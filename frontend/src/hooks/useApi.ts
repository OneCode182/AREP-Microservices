import { useAuth0 } from '@auth0/auth0-react';
import { useCallback } from 'react';

export function useApi() {
  const { getAccessTokenSilently } = useAuth0();
  const baseUrl = import.meta.env.VITE_API_URL;

  const fetchWithAuth = useCallback(async (path: string, options: RequestInit = {}) => {
    const token = await getAccessTokenSilently();
    const res = await fetch(`${baseUrl}${path}`, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    if (!res.ok) throw new Error(`API error: ${res.status}`);
    return res.json();
  }, [getAccessTokenSilently, baseUrl]);

  const fetchPublic = useCallback(async (path: string) => {
    const res = await fetch(`${baseUrl}${path}`);
    if (!res.ok) throw new Error(`API error: ${res.status}`);
    return res.json();
  }, [baseUrl]);

  return { fetchWithAuth, fetchPublic };
}
