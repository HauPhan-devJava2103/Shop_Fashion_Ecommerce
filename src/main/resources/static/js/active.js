(function ($) {
    'use strict';

    var $window = $(window);

    // :: Nav Active Code
    if ($.fn.classyNav) {
        $('#essenceNav').classyNav();
    }

    // :: Sliders Active Code
    if ($.fn.owlCarousel) {
        var $popularProductsSlides = $('.popular-products-slides');

        $popularProductsSlides.owlCarousel({
            items: 4,
            margin: 30,
            loop: true,
            nav: true,
            navText: ["<img src='img/core-img/long-arrow-left.svg' alt=''>", "<img src='img/core-img/long-arrow-right.svg' alt=''>"],
            dots: false,
            autoplay: true,
            autoplayTimeout: 5000,
            autoplayHoverPause: false,
            autoplaySpeed: 900,
            navSpeed: 900,
            smartSpeed: 1000,
            responsive: {
                0: {
                    items: 1
                },
                576: {
                    items: 2
                },
                768: {
                    items: 3
                },
                992: {
                    items: 4
                }
            }
        });

        // Ensure autoplay continues after manual navigation
        $popularProductsSlides.on('click', '.owl-prev, .owl-next', function () {
            $popularProductsSlides.trigger('stop.owl.autoplay');
            $popularProductsSlides.trigger('play.owl.autoplay', [5000]);
        });

        $('.product_thumbnail_slides').owlCarousel({
            items: 1,
            margin: 0,
            loop: true,
            nav: true,
            navText: ["<img src='img/core-img/long-arrow-left.svg' alt=''>", "<img src='img/core-img/long-arrow-right.svg' alt=''>"],
            dots: false,
            autoplay: true,
            autoplayTimeout: 5000,
            smartSpeed: 1000
        });

        // Hero/Welcome Slides
        $('.welcome_slides').owlCarousel({
            items: 1,
            margin: 0,
            loop: true,
            nav: false,
            dots: true,
            autoplay: true,
            autoplayTimeout: 5000,
            smartSpeed: 1000,
            animateIn: 'fadeIn',
            animateOut: 'fadeOut'
        });
    }

    // :: Header Cart Active Code
    var cartbtn1 = $('#essenceCartBtn');
    var cartOverlay = $(".cart-bg-overlay");
    var cartWrapper = $(".right-side-cart-area");
    var cartbtn2 = $("#rightSideCart");
    var cartOverlayOn = "cart-bg-overlay-on";
    var cartOn = "cart-on";

    cartbtn1.on('click', function () {
        cartOverlay.toggleClass(cartOverlayOn);
        cartWrapper.toggleClass(cartOn);
    });
    cartOverlay.on('click', function () {
        $(this).removeClass(cartOverlayOn);
        cartWrapper.removeClass(cartOn);
    });
    cartbtn2.on('click', function () {
        cartOverlay.removeClass(cartOverlayOn);
        cartWrapper.removeClass(cartOn);
    });

    // :: ScrollUp Active Code
    if ($.fn.scrollUp) {
        $.scrollUp({
            scrollSpeed: 1000,
            easingType: 'easeInOutQuart',
            scrollText: '<i class="fa fa-angle-up" aria-hidden="true"></i>'
        });
    }

    // :: Sticky Active Code
    $window.on('scroll', function () {
        if ($window.scrollTop() > 0) {
            $('.header_area').addClass('sticky');
        } else {
            $('.header_area').removeClass('sticky');
        }
    });

    // :: Nice Select Active Code
    if ($.fn.niceSelect) {
        $('select').not('.js-no-nice-select').niceSelect();
    }

    // :: Slider Range Price Active Code
    $('.slider-range-price').each(function () {
        var min = jQuery(this).data('min');
        var max = jQuery(this).data('max');
        var unit = jQuery(this).data('unit');
        var value_min = jQuery(this).data('value-min');
        var value_max = jQuery(this).data('value-max');
        var label_result = jQuery(this).data('label-result');
        var t = $(this);

        var updateRange = function (values) {
            var sliderRange = t.closest('.slider-range');
            if (sliderRange.length) {
                sliderRange.find("input[name='minPrice']").val(values[0]);
                sliderRange.find("input[name='maxPrice']").val(values[1]);

                var result = label_result + " " + values[0] + unit + ' - ' + values[1] + unit;
                sliderRange.find('.range-price').html(result);
            }
        };

        $(this).slider({
            range: true,
            min: min,
            max: max,
            values: [value_min, value_max],
            slide: function (event, ui) {
                updateRange(ui.values);
            }
        });

        // Set initial UI + hidden inputs
        updateRange([value_min, value_max]);
    });

    // :: Favorite Button Active Code
    var favme = $(".favme");

    favme.on('click', function () {
        $(this).toggleClass('active');
    });

    favme.on('click touchstart', function () {
        $(this).toggleClass('is_animating');
    });

    favme.on('animationend', function () {
        $(this).toggleClass('is_animating');
    });

    // :: Nicescroll Active Code
    if ($.fn.niceScroll) {
        $(".cart-list, .cart-content").niceScroll();
    }

    // :: wow Active Code
    if ($window.width() > 767) {
        new WOW().init();
    }

    // :: Tooltip Active Code
    if ($.fn.tooltip) {
        $('[data-toggle="tooltip"]').tooltip();
    }

    // :: PreventDefault a Click
    $("a[href='#']").on('click', function ($) {
        $.preventDefault();
    });

})(jQuery);