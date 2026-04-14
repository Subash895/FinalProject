document.querySelector(".upload").addEventListener("click", () => {
    const input = document.createElement("input");
    input.type = "file";
    input.multiple = true;

    input.onchange = (e) => {
        const gallery = document.getElementById("gallery");

        Array.from(e.target.files).forEach(file => {
            const reader = new FileReader();

            reader.onload = (ev) => {
                const img = document.createElement("img");
                img.src = ev.target.result;
                gallery.appendChild(img);
            };

            reader.readAsDataURL(file);
        });
    };

    input.click();
});