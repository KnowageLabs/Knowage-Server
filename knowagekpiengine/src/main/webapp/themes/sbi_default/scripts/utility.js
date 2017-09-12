$(document).ready(function() { 
		
 
	// ---- Generic Stuff ----------------------------------------------------------------------------------------------------------

	$('.box .fav-container').stop(true,true).hover(function(){
		$(this).find('.fav').animate({
			'height': '62px'
		},150,function(){
			$(this).find('.counter').stop().fadeIn(30);
		});
	},function(){
		$(this).find('.fav .counter').stop().fadeOut(50,function(){
			$(this).parents('.fav').stop().animate({
				'height': '40px'
			},50);
		});
	});
	
	$('.list-container .box:nth-child(3n)').addClass('counter-3');
	$('.list-container .box:nth-child(4n)').addClass('counter-4');
	$('.list-container .box:nth-child(5n)').addClass('counter-5');

	
	// Sort order for datasets list and maps list 
	
	// ATTENTION! DELETED lines from 27 to 48
	
	toggleElements($('.list-actions .order')); // ATTENTION! ADDED
	
	$('.top-menu .reserved span').click(function(){
		reserved	= 	$(this).parents('.reserved');
		loginPanel 	= 	reserved.find('.login-panel');
		/*if(reserved.hasClass('open')){
		
		}else{
			loginPanel.slideDown();
		}*/
		loginPanel.slideToggle();
		reserved.toggleClass('open');
	});
	
	
	// ---- Map Panel   ----------------------------------------------------------------------------------------------------------
	
	
	if($('body').hasClass('map-body')){
	
		$('.panel-actions .btn-toggle').click(function(){
			if($(this).hasClass('open')){
				$('.panel').animate({
					'right'	:	'-362px'
				});
				$(this).removeClass('open');
			}else{
				$('.panel').animate({
					'right'	:	'0'
				});
				$(this).addClass('open');
			}
		});
		
		// Toggle map type
		toggleElements($('.map-type'));
	
	
		$(window).resize(function(){
			panelScroll();
		});
		
		panelScroll();
		
		if(!$('body').hasClass('lte-8')){
			$('#scroll .iScrollIndicator').mousedown(function() {
				$(this).addClass('grabbing');
			});
			$('#scroll .iScrollIndicator').mouseup(function() {
				$(this).removeClass('grabbing');
			});
		}
	}
	
	
	$('input, textarea, button, a, select').off('touchstart mousedown').on('touchstart mousedown', function(e) {
		e.stopPropagation();
	});
	
	accordion($('.group'),'.button','.slider');
	
	$('.radio').each(function(){
		$(this).find('input').each(function(){
			$(this).change(function(){
				$(this).parents('.radio').find('.radio-option').removeClass('checked');
				if($(this).is(':checked')){
					$(this).parents('.radio-option').addClass('checked');
				}
			});
		});
	});
	
	$('.map-tools .map-tools-element').each(function(){
		$(this).find('.tools-content').data('maxWidth',$(this).find('.tools-content').outerWidth()).data('width',$(this).find('.tools-content').width()).data('height',$(this).find('.tools-content').height()).hide();
		$(this).data('originalWidth',$(this).width());
	});
	
	$('.map-tools .icon').click(function(){
		tool = $(this).parents('.map-tools-element');
		height = tool.find('.tools-content').data('height');
		width = tool.find('.tools-content').data('width');
		/*
		if($('body').hasClass('lte-7')){
		}else{*/
			tool.find('.tools-content').css({
				'width':	0,
				'height':	0,
				'display':	'block'
			}).animate({
				'height':	height + 'px',
				'width':	width + 'px'
			},200);
			$(this).hide();
			if(tool.hasClass('layers')){
				contentWidth = tool.find('.tools-content').data('maxWidth');
				tool.animate({
					'width':	contentWidth + 'px'
				},200);
			}
		/*}*/
	});

	$('.map-tools .btn-close').click(function(){
		tool = $(this).parents('.map-tools-element');
		tool.find('.tools-content').fadeOut(200).animate({
			'height':	'0',
			'width':	'0'
		},300);
		tool.find('.icon').show();
		if(tool.hasClass('layers')){
			contentWidth = tool.data('originalWidth');
			tool.animate({
				'width':	contentWidth + 'px'
			},200);
		}
	});
	
	function panelScroll(){
		panelHeight 	= $('.panel').height();
		buttonsHeight	= $('.panel-buttons-container').outerHeight();
		maxHeight		= panelHeight - buttonsHeight - 1;
		$('.panel .scroll').css('max-height', maxHeight + 'px');
		if($('body').hasClass('lte-8')){
			$('#scroll').css('overflow-y','auto');
		}else{
			if($('#scroll').data('initialize') == '1'){
				panelScrollElement.refresh();
			}else{
				panelScrollElement 	= new IScroll('#scroll', { scrollbars: 'custom', mouseWheel: true, interactiveScrollbars: true});
				$('#scroll').data('initialize','1');
			}
			if($('.panel .scroll').height() < maxHeight){
				$('.iScrollVerticalScrollbar').addClass('hidden');
			}else{
				$('.iScrollVerticalScrollbar').removeClass('hidden');
			}
		}
	}
	
	
	function toggleElements(toggleContainer){
		curHeight = toggleContainer.css('height');
		toggleContainer.data('curHeight',curHeight).css('height','auto');
		maxHeight = toggleContainer.css('height');
		toggleContainer.data('maxHeight',maxHeight);
		toggleContainer.css('height',toggleContainer.data('curHeight'));
		toggleContainer.find('a').each(function(){
			$(this).click(function(){
				if(toggleContainer.hasClass('open')){
					toggleContainer.animate({
						'height':	toggleContainer.data('curHeight')
					});
					toggleContainer.removeClass('open');
				}else{
					toggleContainer.animate({
						'height':	toggleContainer.data('maxHeight')
					});
					toggleContainer.addClass('open');
				}
				toggleContainer.find('li').removeClass('active last').find('.arrow').remove();
				$(this).parents('li').addClass('active').prependTo(toggleContainer).find('a').append('<span class="arrow" />');
				toggleContainer.find('li:last').addClass('last');
				if($('body').hasClass('map-body')){
					setTimeout(function(){
						panelScroll();
					},400);
				}
			});
		});
	}
	
	function accordion(list,button,slider){
		list.each(function(){
			$(this).find(button).click(function(){
				$(this).parents('li').toggleClass('open').find(slider).slideToggle();
				if($('body').hasClass('map-body')){
					setTimeout(function(){
						panelScroll();
					},400);
				}
			});
		});
	}
	
	

	
	// ---- PreFilled   ----------------------------------------------------------------------------------------------------------
			
	$.fn.preFilled = function() {
	$(this).focus(function(){
		if( this.value == this.defaultValue ) {
			this.value = "";
		}				   
	}).blur(function(){
		if( !this.value.length ) {
			this.value = this.defaultValue;
		}
	});
	};
	
	$(".list-actions .search-form .field input,#l-password,#l-username").preFilled();


	// ---- CSS PIE - Round Corners  ----------------------------------------------------------------------------------------------------------
	
	if($('body').hasClass('lte-8')){
		$('.list-actions .btn-add').addClass('corner-type-a');
		$('.corner-type-a').each(function(){
			$(this).append('<span class="corner-a corner-tl" /><span class="corner-a corner-tr" /><span class="corner-a corner-bl" /><span class="corner-a corner-br" />');
		});
	}

	if($('body').hasClass('lte-8')){
		if(window.PIE){
			$('.login-panel .submit input,.login-panel .field input,.panel-actions li.first,.panel-actions li.last,.panel-actions,.list-actions .search-form,.main-buttons a,.list-tab,.list-actions .order,.panel .btn-1,.panel .btn-2,.map-tools .btn-close').not('.panel .indicators .btn-2').each(function(){
				PIE.attach(this);
			});
		}
	}
	
	if($('body').hasClass('ie-8')){
		if(window.PIE){
			$('.box').each(function(){
				PIE.attach(this);
			});
		}
	}

});	