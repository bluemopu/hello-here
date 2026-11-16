const dialog = document.querySelector('dialog');

const openModalButton = document.querySelector('.open_modal');
openModalButton.addEventListener('click', () => {
    dialog.showModal();
});

const closeModalButton = document.getElementById('close_modal');
closeModalButton.addEventListener('click', () => {
    dialog.close();
});