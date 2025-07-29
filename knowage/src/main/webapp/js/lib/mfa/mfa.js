function validateForm() {
  const code = document.forms["myForm"]["code"].value;
  if (code.trim() === "") {
    alert("Inserisci il codice one-time.");
    return false;
  }
  return true;
}
function copyToClipboard() {
  const secretElement = document.querySelector(".secret span");
  const secretText = secretElement.textContent;

  navigator.clipboard.writeText(secretText);
}
