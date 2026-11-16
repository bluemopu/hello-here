(function () {
  const tokenElement = document.querySelector('meta[name="_csrf"]');
  const headerElement = document.querySelector('meta[name="_csrf_header"]');

  if (!tokenElement || !headerElement) {
    throw new Error("CSRF token is not available");
  }

  const token = tokenElement.content;
  const headerName = headerElement.content;

  window.csrfFetch = function (input, init = {}) {
    const headers = new Headers(init.headers || {});

    headers.set(headerName, token);

    return fetch(input, {
      ...init,
      headers: headers,
    });
  };
})();
