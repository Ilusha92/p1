$(document).ready(function() {
    var periods = $('#staff-info').data('periods');
    var sameEquipmentForAllDays = $('#staff-info').data('same-equipment-for-all-days');
    var staff = $('#staff-info').data('staff');

    console.log(periods);
    console.log(sameEquipmentForAllDays);
    console.log(staff);

        function checkHiddenClasses() {
            for (let i = 0; i < 10; i++) {
                let row = $('#staff-row-' + i);
                console.log('Row ' + i + ' has class hidden:', row.hasClass('hidden'));
            }
        }

        function showNextStaffRow() {
            $('.staff-row.hidden:first').removeClass('hidden');
            toggleRemoveButtons();
            toggleAddButtons();
            checkHiddenClasses();
        }

        function hideLastStaffRow() {
            $('.staff-row:not(.hidden):last').addClass('hidden').find('select, input').val('');
            toggleRemoveButtons();
            toggleAddButtons();
            checkHiddenClasses();
        }

        function toggleRemoveButtons() {
            $('.staff-row').each(function() {
                if (!$(this).hasClass('hidden')) {
                    $(this).find('.remove-staff-btn').show();
                } else {
                    $(this).find('.remove-staff-btn').hide();
                }
            });
        }

        function toggleAddButtons() {
            let allVisible = true;
            $('.staff-row').each(function() {
                if ($(this).hasClass('hidden')) {
                    allVisible = false;
                }
            });

            if (allVisible) {
                $('.add-staff-btn').hide();
            } else {
                $('.add-staff-btn').show();
            }
        }

        // Initial call
        checkHiddenClasses(); // Проверка при загрузке страницы

        // Обработчики событий
        $('.add-staff-btn').click(function() {
            showNextStaffRow();
        });

        $('.remove-staff-btn').click(function() {
            hideLastStaffRow();
        });


});
