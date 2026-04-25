import { useAuth0 } from '@auth0/auth0-react';
import LoginButton from './LoginButton';
import LogoutButton from './LogoutButton';

export default function Navbar() {
  const { isAuthenticated } = useAuth0();
  return (
    <nav className="navbar">
      <span className="navbar-title">Twitter Monolith</span>
      <div className="navbar-actions">
        {isAuthenticated ? <LogoutButton /> : <LoginButton />}
      </div>
    </nav>
  );
}
