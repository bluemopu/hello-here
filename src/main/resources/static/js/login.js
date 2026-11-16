const loginButton = document.getElementById("loginButton");

const signUpButton = document.getElementById("signUpButton");

const result = document.getElementById("result");

loginButton.addEventListener("click", function () {
  const email = document.getElementById("email").value.trim();

  const password = document.getElementById("password").value;

  if (email === "" || password === "") {
    result.textContent = "メールアドレスとパスワードを入力してください";

    return;
  }

  csrfFetch("/login", {
    method: "POST",

    headers: {
      "Content-Type": "application/json",
    },

    body: JSON.stringify({
      email: email,
      password: password,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("ログインに失敗しました");
      }

      return response.text();
    })
    .then(() => {
      window.location.href = "/home";
    })
    .catch((error) => {
      console.error(error);

      result.textContent = "メールアドレスまたはパスワードが違います";
    });
});

signUpButton.addEventListener("click", function () {
  window.location.href = "/register";
});
