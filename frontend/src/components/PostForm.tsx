import { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useApi } from '../hooks/useApi';

interface PostFormProps {
  onPostCreated: () => void;
}

const MAX_CHARS = 140;

export default function PostForm({ onPostCreated }: PostFormProps) {
  const { fetchWithAuth } = useApi();
  const { user } = useAuth0();
  const [content, setContent] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const remaining = MAX_CHARS - content.length;
  const isOver = remaining < 0;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!content.trim() || isOver) return;
    setSubmitting(true);
    setError(null);
    try {
      await fetchWithAuth('/api/posts', {
        method: 'POST',
        body: JSON.stringify({ content, authorName: user?.name || '' }),
      });
      setContent('');
      onPostCreated();
    } catch {
      setError('Failed to post. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="card post-form">
      <h3>New Post</h3>
      <form onSubmit={handleSubmit}>
        <textarea
          className="post-textarea"
          placeholder="What's happening? (140 chars max)"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={141}
          rows={3}
        />
        <div className="post-form-footer">
          <span className={`char-counter ${isOver ? 'over' : 'ok'}`}>
            {remaining}
          </span>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting || !content.trim() || isOver}
          >
            {submitting ? 'Posting...' : 'Post'}
          </button>
        </div>
        {error && <p className="toast">{error}</p>}
      </form>
    </div>
  );
}
