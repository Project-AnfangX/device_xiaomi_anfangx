#
# Copyright (C) 2021 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from tucana device
$(call inherit-product, device/xiaomi/anfangx/device.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_NAME := lineage_anfangx
PRODUCT_DEVICE := anfangx
PRODUCT_BRAND := Xiaomi
PRODUCT_MODEL := IF 1
PRODUCT_MANUFACTURER := Xiaomi

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi

PRODUCT_BUILD_PROP_OVERRIDES += \
    PRIVATE_BUILD_DESC="tucana-user 11 RKQ1.200826.002 V13.0.2.0.RFDMIXM release-keys"

BUILD_FINGERPRINT := Xiaomi/tucana/tucana:11/RKQ1.200826.002/V13.0.2.0.RFDMIXM:user/release-keys

# RisingOS Config
RISING_MAINTAINER="uwugl"
PRODUCT_BUILD_PROP_OVERRIDES += \
    RisingChipset="Qualcomm Snapdragon 8 Gen 114514" \
    RisingMaintainer="uwugl"
TARGET_PREBUILT_LAWNCHAIR_LAUNCHER := false
WITH_GMS := true
TARGET_DEFAULT_PIXEL_LAUNCHER := true
RISING_PACKAGE_TYPE := GAPPS
