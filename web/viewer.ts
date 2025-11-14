const img = document.getElementById('frame') as HTMLImageElement;
const fpsSpan = document.getElementById('fps')!;
const resSpan = document.getElementById('res')!;

// Embedded tiny PNG sample (replace with a real processed frame as needed)
const dataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=";

function init() {
  img.src = dataUrl;
  fpsSpan.textContent = "30 (sample)";
  resSpan.textContent = "100x100 (sample)";
}

init();
