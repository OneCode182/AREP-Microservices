import { useEffect, useState } from 'react';
import { useApi } from '../hooks/useApi';

interface UserInfo {
  sub: string;
  email: string;
  name: string;
}

export default function UserProfile() {
  const { fetchWithAuth } = useApi();
  const [user, setUser] = useState<UserInfo | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchWithAuth('/api/me')
      .then((data: UserInfo) => setUser(data))
      .catch(() => setError('Failed to load profile'));
  }, [fetchWithAuth]);

  if (error) return <div className="card user-profile"><span className="toast">{error}</span></div>;
  if (!user) return null;

  return (
    <div className="card user-profile">
      <h3>Your Profile</h3>
      <div className="user-info">
        <span className="user-name">{user.name}</span>
        <span className="user-email">{user.email}</span>
      </div>
    </div>
  );
}
