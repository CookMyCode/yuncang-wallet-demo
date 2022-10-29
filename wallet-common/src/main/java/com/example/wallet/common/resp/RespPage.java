package com.example.wallet.common.resp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RespPage<T> {

    private Long total;

    private Integer page;

    private Integer size;

    private List<T> records;

}
