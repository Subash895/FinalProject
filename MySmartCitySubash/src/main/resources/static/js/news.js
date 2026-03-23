document.addEventListener("DOMContentLoaded", () => {

    document.getElementById("newsForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const title = document.getElementById("title").value;
        const content = document.getElementById("content").value;

        await apiRequest("/news", "POST", { title, content });

        loadNews();
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadNews);
});

async function loadNews() {
    const data = await apiRequest("/news");

    document.getElementById("list").innerHTML =
        data.map(n => `<div class="card">${n.title}</div>`).join("");
}