const map = L.map("map");

L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
  maxZoom: 19,
  attribution: "© OpenStreetMap",
}).addTo(map);

navigator.geolocation.getCurrentPosition(
  function (position) {
    const latitude = position.coords.latitude;
    const longitude = position.coords.longitude;

    map.setView([latitude, longitude], 17);

    L.marker([latitude, longitude]).addTo(map).bindPopup("現在地").openPopup();

    L.circle([latitude, longitude], {
      radius: 100,
    }).addTo(map);
  },

  function (error) {
    console.error("位置情報を取得できませんでした", error);
  },
);
