from pathlib import Path
from yaml import safe_load
from fastapi import APIRouter, Request
from fastapi.responses import HTMLResponse, JSONResponse

router = APIRouter(tags=["AsyncAPI"])

SPEC_PATH = Path(__file__).parent / "asyncapi.yaml"

_spec: dict | None = None


def load_spec() -> dict:
    global _spec
    if _spec is None:
        with open(SPEC_PATH) as f:
            _spec = safe_load(f)
    return _spec


@router.get("/asyncapi.json", include_in_schema=False)
async def get_asyncapi_spec():
    return JSONResponse(load_spec())


@router.get("/asyncapi", include_in_schema=False)
async def get_asyncapi_ui(request: Request):
    ws_url = f"{request.base_url.scheme.replace('https', 'wss').replace('http', 'ws')}://{request.base_url.netloc}/ws"
    return HTMLResponse(
        f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>QuietSpace — AsyncAPI Docs</title>
  <link rel="stylesheet" href="https://unpkg.com/@asyncapi/react-component@3.3.0/styles/colony.css">
  <style>
    body {{ margin: 0; padding: 0; font-family: sans-serif; }}
    #root {{ max-width: 1200px; margin: 0 auto; padding: 20px; }}
  </style>
</head>
<body>
  <div id="root"></div>
  <script src="https://unpkg.com/@asyncapi/react-component@3.3.0/browser/standalone/index.js"></script>
  <script>
    fetch('/asyncapi.json')
      .then(r => r.json())
      .then(spec => AsyncApiStandalone.render({{ schema: spec, config: {{ show: {{ sidebar: true }} }} }}, document.getElementById('root')));
  </script>
</body>
</html>"""
    )
