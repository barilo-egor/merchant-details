package net.rcetech.md.integrations.platform.goatx;

import lombok.Data;

@Data
public class Request {

    private String contract;

    private String invid;

    private String sum;

    private String signature;

    private Method way;
}
