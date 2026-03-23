document.addEventListener("DOMContentLoaded", function () {

    console.log("FORUM JS LOADED"); // 🔥 check

    const form = document.getElementById("forumForm");

    if (!form) {
        console.error("Form NOT FOUND");
        return;
    }

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        console.log("FORM SUBMIT WORKING"); // 🔥 check

        const title = document.getElementById("title").value;
        const content = document.getElementById("content").value;

        try {
            await apiRequest("/forumposts", "POST", {
                title,
                content
            });

            alert("Post Added");

            loadPosts();

        } catch (err) {
            console.error(err);
            alert("Error adding post");
        }
    });

    document.getElementById("loadBtn").addEventListener("click", loadPosts);

});

async function loadPosts() {

    const data = await apiRequest("/forumposts");

    document.getElementById("list").innerHTML =
        data.map(p => `
            <div class="card">
                <h3>${p.title}</h3>
                <p>${p.content}</p>
            </div>
        `).join("");
}