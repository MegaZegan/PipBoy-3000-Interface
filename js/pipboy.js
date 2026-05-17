$(document).ready(function () {
    var weapons = [
        {
            "name": "44_pistol",
            "damage": 48,
            "fire_rate": 6,
            "range": 119,
            "accuracy": 66,
            "weight": 4.2,
            "value": 99
        },
        {
            "name": "10mm_pistol",
            "damage": 18,
            "fire_rate": 48,
            "range": 119,
            "accuracy": 61,
            "weight": 4.2,
            "value": 53
        },
        {
            "name": "assault_rifle",
            "damage": 30,
            "fire_rate": 40,
            "range": 119,
            "accuracy": 72,
            "weight": 13.1,
            "value": 144
        }
    ];

    $('.item-list a').on('mouseenter', function (e) {
        var currentItem = $(e.currentTarget).attr('class');
        weapons.forEach(item => {
            if (item.name == currentItem) {
                console.log(item);
                var container = $('.item-stats');
                container.find('.damage').html(item.damage);
                container.find('.ammo').html('500');
                container.find('.fire_rate').html(item.fire_rate);
                container.find('.range').html(item.range);
                container.find('.accuracy').html(item.accuracy);
                container.find('.weight').html(item.weight);
                container.find('.value').html(item.value);
            }
        });
        console.log(currentItem);
    });
    $('.item-list a').on('mouseleave', function () {
        temizle();
    })

    $('.item-list a').on('click', function (e) {
        $('.item-list a').removeClass('active');
        $(e.currentTarget).addClass('active');
        var currentItem = $(e.currentTarget).attr('class');
        weapons.forEach(item => {
            if (item.name == currentItem) {
                console.log(item);
                var container = $('.item-stats');
                container.find('.damage').html(item.damage);
                container.find('.ammo').html('500');
                container.find('.fire_rate').html(item.fire_rate);
                container.find('.range').html(item.range);
                container.find('.accuracy').html(item.accuracy);
                container.find('.weight').html(item.weight);
                container.find('.value').html(item.value);
            }
        });
    });
});