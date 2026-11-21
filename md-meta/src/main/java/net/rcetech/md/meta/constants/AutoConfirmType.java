package net.rcetech.md.meta.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AutoConfirmType {
    AUTO_WITHDRAWAL("Автовывод"),
    ADD_TO_POOL("Пул");

    private final String description;
}
