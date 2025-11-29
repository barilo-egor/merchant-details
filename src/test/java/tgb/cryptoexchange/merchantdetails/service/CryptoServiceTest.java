package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.Test;

class CryptoServiceTest {

    private CryptoService cryptoService = new CryptoService();

    @Test
    void test() throws Exception {
        System.out.println(cryptoService.decrypt(
                "4jIOwOfFTB7OY4pqXEDaa6EXekvAPBNUZOABYXXvHZc=", "gAAAAABpKn72W2jpm24pkZ_atASJu52w3OijKaALAraDW_V1sPsyRFBidfAHCEjKoswGoBrQm5eBsuSz5kIxyMd3_ZIfTwEi4OZ2YMywo5RPssUyu3Ta35XNMOyOJlK46ArpBUn33aN62j0d0CG0rxYeFMpVb9LgP4Mb0RqRv5j-th-xhABV2DLqBmb7HKWcVLhD7mUpiFa6JVIVdD64IW3ttUrP_1anQHQhCxvKWsEECgwY4TTdir5rUrFIJ83yF9lCbkd6FSSstx3qDgo1z44iqA0JUFMc0jLuI62DPj5fgrExWzO0Bju98-_1jrD1phatqeRyL5jIDcnQehhG20i2p9MNkBV0QejbrNDgxQXq5Op1OMd_JZ5Tbu65a4jnOUHPI9duGRooPkSGsuj5OxnAxGuphDcut_fOloDf-R7mrA-WG3WO74kTOOysxURMwbq0DZgwyxnWgWagYGxZ9rSHDAYySdacYodUSOAvpTdsTxrSHpAulOJ98Mu3IC2atI6KbqpsRzUqgZaXMrqSZCbZvdFMRJyFVDSzxpBJ4tuhtaO5TjOGbLICxHf1ciznJQl9E-fx-tWWZWMUTdPw5jJWb0miWOX4T_2Bam7L0uvaE8o0WxHHchRs1tV3glI_CtfOIwZzryMU74G0_dp7D_kWboAGrZVnEuawQbBWPGQlcWucR5M4CvZzMEnfWO90zkinnyld0in4")
        );
    }
}