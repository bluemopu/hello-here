(function () {
  const mapElement = document.getElementById("map");
  const mapStatus = document.getElementById("mapStatus");

  function showMapError(message) {
    mapElement.classList.add("map_unavailable");
    mapElement.setAttribute("aria-label", message);
    mapElement.textContent = "地図を表示できません";
    mapStatus.textContent = message;
  }

  if (typeof window.L === "undefined") {
    showMapError("地図を読み込めませんでした");
    return;
  }

  if (!navigator.geolocation) {
    showMapError("このブラウザでは位置情報を利用できません");
    return;
  }

  const map = L.map("map");

  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "© OpenStreetMap",
  }).addTo(map);

  navigator.geolocation.getCurrentPosition(
    function (position) {
      const latitude = position.coords.latitude;
      const longitude = position.coords.longitude;

      mapStatus.textContent = "";
      map.setView([latitude, longitude], 17);

      L.marker([latitude, longitude])
        .addTo(map)
        .bindPopup("現在地")
        .openPopup();

      L.circle([latitude, longitude], {
        radius: 100,
      }).addTo(map);
    },

    function (error) {
      console.error("位置情報を取得できませんでした", error);

      map.remove();
      showMapError(
        "位置情報を取得できませんでした。ブラウザの許可設定を確認してください",
      );
    },
    { timeout: 10000, maximumAge: 30000 },
  );
})();
