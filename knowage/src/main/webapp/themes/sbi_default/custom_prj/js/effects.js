$(document).ready(function () {

  $("a.link-to-secondary").click(function () {
    $("#cards-main").fadeOut();
    $("#cards-secondary").delay(500).fadeIn();
  });

  $("a.return").click(function () {
    $("#cards-secondary").fadeOut();
    $("#cards-main").delay(500).fadeIn();
  });

  $('.carousel .carousel-item').each(function(){
    var next = $(this).next();
    if (!next.length) {
      next = $(this).siblings(':first');
    }
    next.children(':first-child').clone().appendTo($(this));

    /*for (var i=0;i<2;i++) {
        next=next.next();
        if (!next.length) {
          next = $(this).siblings(':first');
        }
        next.children(':first-child').clone().appendTo($(this));
      }*/
});









});
