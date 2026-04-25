import { useCallback, useEffect, useState } from 'react';
import { useApi } from '../hooks/useApi';

interface Post {
  id: number;
  content: string;
  authorName: string;
  createdAt: string;
}

interface StreamFeedProps {
  refreshKey: number;
}

export default function StreamFeed({ refreshKey }: StreamFeedProps) {
  const { fetchPublic } = useApi();
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchPublic('/api/stream')
      .then((data: Post[]) => setPosts(data))
      .catch(() => setError('Failed to load stream.'))
      .finally(() => setLoading(false));
  }, [fetchPublic]);

  useEffect(() => {
    load();
  }, [load, refreshKey]);

  return (
    <div className="stream-feed">
      <div className="stream-header">
        <h2>Public Stream</h2>
        <button className="btn btn-secondary" onClick={load} disabled={loading}>
          {loading ? 'Loading...' : 'Refresh'}
        </button>
      </div>
      {error && <p className="toast">{error}</p>}
      <div className="posts-list">
        {posts.length === 0 && !loading && (
          <p className="empty-feed">No posts yet. Be the first!</p>
        )}
        {posts.map((post) => (
          <div key={post.id} className="post-card">
            <p className="post-author">{post.authorName}</p>
            <p className="post-content">{post.content}</p>
            <p className="post-date">
              {new Date(post.createdAt).toLocaleString()}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
