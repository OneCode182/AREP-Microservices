import { useAuth0 } from '@auth0/auth0-react';

export default function LogoutButton() {
  const { logout } = useAuth0();
  return (
    <button
      className="btn btn-danger"
      onClick={() =>
        logout({
          logoutParams: {
            returnTo:
              window.location.origin +
              (window.location.pathname === '/' &&
              window.location.hostname.includes('amazonaws')
                ? '/index.html'
                : window.location.pathname),
          },
        })
      }
    >
      Log Out
    </button>
  );
}
