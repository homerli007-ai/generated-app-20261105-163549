# Safe File Deleter

A small full-stack demo application that lets you delete files inside a sandboxed "allowed root" directory via a web UI. The backend enforces path-traversal protection, symlink refusal, and regular-file-only deletion.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

### Backend

```bash
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Files are deleted relative to `app.allowed-root` (default `./allowed`, resolved relative to the backend working directory). Create this directory before use:

```bash
mkdir -p backend/allowed
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server runs on `http://localhost:5173` and proxies `/api` requests to the backend on port `8080`.

## API

### `POST /api/delete`

Deletes one or more files under the allowed root.

**Request body**

```json
{
  "paths": ["a.txt", "subdir/b.txt"]
}
```

Paths are interpreted relative to `app.allowed-root`.

**Response**

```json
{
  "deleted": ["a.txt"],
  "errors": [
    { "path": "subdir/b.txt", "error": "not found" }
  ]
}
```

Each path is validated independently. Possible per-path errors include:

- `outside allowed root` — path resolves outside the configured root
- `symlink refused` — target is a symbolic link
- `not found` — target does not exist
- `not a regular file` — target is a directory or special file
- I/O error message — any `IOException` raised during deletion

## Notes

- The allowed root is configured via `app.allowed-root` in `backend/src/main/resources/application.properties` (default `./allowed`).
- Only regular files are deleted; directories are rejected with `not a regular file`.
- Symbolic links are refused to prevent escaping the sandbox via link traversal.
- Paths are normalized and checked with `startsWith(root)` to block `..` traversal.
- There is no authentication or authorization — this is a demo and should not be exposed to untrusted networks.
- The frontend has no build-time configuration; it relies on the Vite dev proxy. For production, serve the built `frontend/dist` behind a reverse proxy that forwards `/api` to the backend.
- Deletions are permanent; there is no trash or undo.