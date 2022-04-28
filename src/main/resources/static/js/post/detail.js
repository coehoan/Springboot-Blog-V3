$("#btn-delete").click(() => {
    postDelete();
});

async function postDelete() {
    let id = $("#post-id").val();
    let pageOwnerId = $("#pageOwner-id").val();
    let response = await fetch(`/s/api/post/${id}/delete`, {
        method: "DELETE"
    });

    if (response.status == 200) {
        alert("삭제 완료");
        location.href = `/user/${pageOwnerId}/post`;
    } else {
        alert("삭제 실패");
        history.back();
    }
}