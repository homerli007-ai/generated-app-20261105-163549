import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    if (paths.length === 0) return;
    setLoading(true);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paths })
      });
      setResult(await res.json());
    } catch (err) {
      setResult({ error: String(err) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <h1>Safe File Deleter</h1>
      <p>Enter file paths (relative to allowed root), one per line.</p>
      <form onSubmit={submit}>
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          rows={8}
          placeholder="a.txt\nb.txt"
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Deleting...' : 'Delete'}
        </button>
      </form>
      {result && (
        <div className="result">
          <h2>Result</h2>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}
