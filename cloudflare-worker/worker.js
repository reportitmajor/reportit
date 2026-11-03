/**
 * Report-It Cloudflare Worker: Automated Civic Push Notification Dispatcher
 *
 * Integrates with OneSignal REST API (v2) to automatically dispatch push notifications
 * to citizens and municipal field officers whenever a civic report is submitted, updated, or resolved.
 */

const ONESIGNAL_APP_ID = "dc639f71-d339-4858-ab5b-05e1e26e221e";
const ONESIGNAL_API_KEY = "YOUR_ONESIGNAL_REST_API_KEY";

// CORS Headers for secure cross-origin requests
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
};

export default {
  async fetch(request, env, ctx) {
    // 1. Handle CORS Preflight
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    const url = new URL(request.url);

    // 2. Health check route
    if (request.method === "GET" && (url.pathname === "/" || url.pathname === "/health")) {
      return new Response(
        JSON.stringify({
          status: "online",
          service: "Report-It Civic Push Notification Worker",
          app_id: ONESIGNAL_APP_ID,
          timestamp: new Date().toISOString()
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 3. Dispatch Notification Endpoint
    if (request.method === "POST" && (url.pathname === "/notify" || url.pathname === "/new-report" || url.pathname === "/status-update")) {
      try {
        const body = await request.json();

        const reportId = body.reportId || `RI-${Date.now().toString().slice(-6)}`;
        const category = body.category || "Civic Hazard";
        const wardName = body.wardName || "Bengaluru Ward";
        const severity = body.severity || "Medium";
        const status = body.status || "Submitted";
        const targetUserId = body.userId; // optional: if targeting specific citizen
        const customMessage = body.message;

        let heading = "";
        let content = "";

        if (url.pathname === "/status-update" || body.type === "STATUS_UPDATE") {
          heading = `⚡ Report #${reportId} Status: ${status}`;
          content = customMessage || `Your ${category} grievance at ${wardName} was updated to: ${status}.`;
        } else {
          // New Report
          const icon = getCategoryIcon(category);
          heading = `${icon} New Civic Report: ${category}`;
          content = customMessage || `Report #${reportId} lodged in ${wardName}. Severity: ${severity}. AI Verified.`;
        }

        // Build OneSignal Payload
        const oneSignalPayload = {
          app_id: env.ONESIGNAL_APP_ID || ONESIGNAL_APP_ID,
          headings: { en: heading },
          contents: { en: content },
          data: {
            reportId: reportId,
            category: category,
            wardName: wardName,
            status: status,
            click_action: "OPEN_REPORT_TRACKING"
          },
          // Android specific visual badges
          small_icon: "ic_launcher",
          priority: severity.toLowerCase().includes("critical") || severity.toLowerCase().includes("high") ? 10 : 5
        };

        // If targetUserId is provided, send targeted notification to that user's external_id
        if (targetUserId) {
          oneSignalPayload.include_aliases = {
            external_id: [targetUserId]
          };
          oneSignalPayload.target_channel = "push";
        } else {
          // Otherwise broadcast to all subscribed users (or ward segment)
          oneSignalPayload.included_segments = ["Total Subscriptions"];
        }

        // Send POST request to OneSignal REST API v2
        const response = await fetch("https://api.onesignal.com/notifications", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Key ${env.ONESIGNAL_API_KEY || ONESIGNAL_API_KEY}`
          },
          body: JSON.stringify(oneSignalPayload)
        });

        const result = await response.json();

        return new Response(
          JSON.stringify({
            success: response.ok,
            onesignal_response: result,
            sent_heading: heading,
            sent_content: content
          }),
          {
            status: response.status,
            headers: { ...corsHeaders, "Content-Type": "application/json" }
          }
        );

      } catch (err) {
        return new Response(
          JSON.stringify({ error: err.message }),
          { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    }

    return new Response(
      JSON.stringify({ error: "Route not found. Use POST /notify or POST /new-report" }),
      { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
};

function getCategoryIcon(category) {
  const c = (category || "").toLowerCase();
  if (c.includes("pothole") || c.includes("road") || c.includes("crack")) return "🛣️";
  if (c.includes("garbage") || c.includes("waste")) return "🗑️";
  if (c.includes("light") || c.includes("street")) return "💡";
  if (c.includes("water") || c.includes("leak") || c.includes("drain")) return "💧";
  if (c.includes("electric") || c.includes("transform") || c.includes("power")) return "⚡";
  return "🚨";
}
