package com.bocoo.common.oss.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OssCredentialModeTest {

    @Test
    void blankModeKeepsLegacyAccessKeyBehavior() {
        assertThat(OssCredentialMode.from(null)).isEqualTo(OssCredentialMode.ACCESS_KEY);
        assertThat(OssCredentialMode.from(" ")).isEqualTo(OssCredentialMode.ACCESS_KEY);
    }

    @Test
    void temporaryModesUseExistingSharedBucket() {
        assertThat(OssCredentialMode.from("local_sts").usesSharedBucket()).isTrue();
        assertThat(OssCredentialMode.from("ecs_ram_role").usesSharedBucket()).isTrue();
        assertThat(OssCredentialMode.from("access_key").usesSharedBucket()).isFalse();
    }

    @Test
    void unknownModeIsRejected() {
        assertThatThrownBy(() -> OssCredentialMode.from("unknown"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
