import { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import Navbar from './components/Navbar';
import PostForm from './components/PostForm';
import StreamFeed from './components/StreamFeed';
import UserProfile from './components/UserProfile';

export default function App() {
  const { isLoading, isAuthenticated } = useAuth0();
  const [refreshKey, setRefreshKey] = useState(0);

  if (isLoading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <>
      <Navbar />
      <main className="main-content">
        {isAuthenticated && (
          <>
            <UserProfile />
            <PostForm onPostCreated={() => setRefreshKey((k) => k + 1)} />
          </>
        )}
        <StreamFeed refreshKey={refreshKey} />
      </main>
    </>
  );
}
