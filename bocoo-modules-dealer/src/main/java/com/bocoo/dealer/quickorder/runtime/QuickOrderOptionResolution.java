package com.bocoo.dealer.quickorder.runtime;

import java.util.Map;

record QuickOrderOptionResolution(Map<String, String> selections, String summaryCn,
                                  String summaryEn, boolean motorized) {
}
